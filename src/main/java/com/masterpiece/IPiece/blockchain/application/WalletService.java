package com.masterpiece.IPiece.blockchain.application;

import com.masterpiece.IPiece.blockchain.api.dto.request.KrwtBurnRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.KrwtMintRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.TransactionQueryRequest;
import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBurnResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtMintResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.MyWalletResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TransactionListResponse;
import com.masterpiece.IPiece.blockchain.domain.KrwtOperation;
import com.masterpiece.IPiece.blockchain.domain.OperationType;
import com.masterpiece.IPiece.blockchain.domain.TransactionStatus;
import com.masterpiece.IPiece.blockchain.infra.jpa.KrwtOperationRepository;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.account.VirtualAccountJournal;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountJournalRepository;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.integration.besu.BesuClient;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.mypage.infra.HoldingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final VirtualAccountRepository virtualAccountRepository;
    private final HoldingsRepository holdingsRepository;
    private final KrwtOperationRepository krwtOperationRepository;
    private final VirtualAccountJournalRepository virtualAccountJournalRepository;
    private final BesuClient besuClient;

    private static final long MAX_MINT_AMOUNT = 1_000_000_000L;

    public MyWalletResponse getMyWallet(Long userId) {
        VirtualAccount virtualAccount = virtualAccountRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User " + userId + " has no virtual account"));

        List<Holdings> holdings = holdingsRepository.findAllByVirtualAccount(virtualAccount);

        List<MyWalletResponse.TokenInfo> tokenInfos = holdings.stream()
                .map(holding -> {
                    BigDecimal balance = new BigDecimal(holding.getQuantity());
                    BigDecimal totalSupply = new BigDecimal(holding.getProduct().getTotalTokenQuantity());
                    BigDecimal sharePercentage = totalSupply.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                            balance.divide(totalSupply, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

                    return MyWalletResponse.TokenInfo.builder()
                            .projectId(holding.getProduct().getProductId())
                            .projectName(holding.getProduct().getProductName())
                            .tokenAddress(null)
                            .symbol(holding.getProduct().getTokenName())
                            .balance(holding.getQuantity())
                            .totalSupply(holding.getProduct().getTotalTokenQuantity())
                            .sharePercentage(sharePercentage.toPlainString() + "%")
                            .totalDividendsReceived(0L)
                            .build();
                })
                .collect(Collectors.toList());

        long totalHoldingsValue = holdings.stream()
                .mapToLong(holding -> holding.getQuantity() * holding.getProduct().getCurrentPrice())
                .sum();

        long totalValueKrw = totalHoldingsValue + virtualAccount.getBalanceKrw();

        return MyWalletResponse.builder()
                .walletAddress(virtualAccount.getWalletAddress())
                .balanceKrw(virtualAccount.getBalanceKrw())
                .createdAt(virtualAccount.getCreatedAt())
                .tokens(tokenInfos)
                .totalValueKrw(totalValueKrw)
                .build();
    }

    public TransactionListResponse getTransactions(Long userId, TransactionQueryRequest request) {
        virtualAccountRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User " + userId + " has no virtual account"));

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("createdAt").descending());

        Page<KrwtOperation> operationsPage;

        if (!"ALL".equalsIgnoreCase(request.getType())) {
            OperationType operationType = OperationType.valueOf(request.getType().toUpperCase());
            operationsPage = krwtOperationRepository.findByUserUserIdAndOperationType(userId, operationType, pageable);
        } else {
            // 거래내역에서는 MINT/BURN(입출금)을 DB 레벨에서 제외
            operationsPage = krwtOperationRepository.findByUserUserIdAndOperationTypeNotIn(
                    userId,
                    List.of(OperationType.MINT, OperationType.BURN),
                    pageable
            );
        }

        List<TransactionListResponse.TransactionItem> transactionItems = operationsPage.getContent().stream()
                .map(op -> TransactionListResponse.TransactionItem.builder()
                        .journalId(op.getOperationId())
                        .type(op.getOperationType().name())
                        .amount(op.getAmount().longValueExact())
                        .balanceAfter(op.getAfterBalance().longValueExact())
                        .memo(op.getMemo())
                        .transactionHash(op.getTxHash())
                        .createdAt(op.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return TransactionListResponse.builder()
                .totalElements(operationsPage.getTotalElements())
                .totalPages(operationsPage.getTotalPages())
                .currentPage(request.getPage())
                .pageSize(request.getSize())
                .transactions(transactionItems)
                .build();
    }

    @Transactional
    public KrwtMintResponse mintKrwt(Long adminUserId, KrwtMintRequest request) {
        if (request.getAmount() <= 0 || request.getAmount() > MAX_MINT_AMOUNT) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "발행 금액은 0보다 크고 " + MAX_MINT_AMOUNT + "이하여야 합니다.");
        }

        VirtualAccount targetAccount = virtualAccountRepository.findByUser_UserId(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "입금 받을 사용자를 찾을 수 없습니다."));

        long previousBalance = targetAccount.getBalanceKrw();

        KrwtOperation pendingOp = KrwtOperation.builder()
                .user(targetAccount.getUser())
                .virtualAccount(targetAccount)
                .accountId(targetAccount.getAccountId())
                .operationType(OperationType.MINT)
                .amount(BigDecimal.valueOf(request.getAmount()))
                .beforeBalance(BigDecimal.valueOf(previousBalance))
                .afterBalance(BigDecimal.valueOf(previousBalance + request.getAmount()))
                .status(TransactionStatus.PENDING)
                .memo(request.getMemo())
                .createdAt(OffsetDateTime.now())
                .build();
        krwtOperationRepository.save(pendingOp);

        String txHash;
        try {
            txHash = besuClient.mintKrwt(targetAccount.getWalletAddress(), BigInteger.valueOf(request.getAmount()));

            targetAccount.increaseBalanceKrw(request.getAmount());
            virtualAccountRepository.save(targetAccount);

            pendingOp.updateStatus(TransactionStatus.SUCCESS, txHash, OffsetDateTime.now());
            krwtOperationRepository.save(pendingOp);

            // 가상계좌 저널 기록 (입금)
            VirtualAccountJournal journal = VirtualAccountJournal.builder()
                    .virtualAccount(targetAccount)
                    .txType("DEPOSIT")
                    .amountKrw(request.getAmount())
                    .balanceAfter(targetAccount.getBalanceKrw())
                    .description("KRWT 입금")
                    .build();
            virtualAccountJournalRepository.save(journal);

        } catch (Exception e) {
            pendingOp.updateStatus(TransactionStatus.FAILED, null, OffsetDateTime.now());
            krwtOperationRepository.save(pendingOp);
            log.error("Failed to mint KRWT on blockchain for user {}: {}", request.getUserId(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.BLOCKCHAIN_TRANSACTION_FAILED, "KRWT 발행 블록체인 트랜잭션 실패", e);
        }

        return KrwtMintResponse.builder()
                .transactionId(pendingOp.getOperationId())
                .userId(request.getUserId())
                .previousBalance(previousBalance)
                .mintAmount(request.getAmount())
                .newBalance(targetAccount.getBalanceKrw())
                .transactionHash(txHash)
                .completedAt(pendingOp.getCompletedAt())
                .build();
    }

    @Transactional
    public KrwtBurnResponse burnKrwt(Long adminUserId, Long targetUserId, KrwtBurnRequest request) {
        if (request.getAmount() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "출금 금액은 0보다 커야 합니다.");
        }

        VirtualAccount userAccount = virtualAccountRepository.findByUser_UserId(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자 지갑을 찾을 수 없습니다."));

        if (userAccount.getBalanceKrw() < request.getAmount()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE, "잔고가 부족합니다.");
        }

        long previousBalance = userAccount.getBalanceKrw();

        KrwtOperation pendingOp = KrwtOperation.builder()
                .user(userAccount.getUser())
                .virtualAccount(userAccount)
                .accountId(userAccount.getAccountId())
                .operationType(OperationType.BURN)
                .amount(BigDecimal.valueOf(request.getAmount()))
                .beforeBalance(BigDecimal.valueOf(previousBalance))
                .afterBalance(BigDecimal.valueOf(previousBalance - request.getAmount()))
                .status(TransactionStatus.PENDING)
                .memo(request.getMemo())
                .createdAt(OffsetDateTime.now())
                .build();
        krwtOperationRepository.save(pendingOp);

        String txHash;
        try {
            txHash = besuClient.burnKrwt(userAccount.getWalletAddress(), BigInteger.valueOf(request.getAmount()));

            userAccount.decreaseBalanceKrw(request.getAmount());
            virtualAccountRepository.save(userAccount);

            pendingOp.updateStatus(TransactionStatus.SUCCESS, txHash, OffsetDateTime.now());
            krwtOperationRepository.save(pendingOp);

            // 가상계좌 저널 기록 (출금)
            VirtualAccountJournal journal = VirtualAccountJournal.builder()
                    .virtualAccount(userAccount)
                    .txType("WITHDRAW")
                    .amountKrw(-request.getAmount())
                    .balanceAfter(userAccount.getBalanceKrw())
                    .description("KRWT 출금")
                    .build();
            virtualAccountJournalRepository.save(journal);

        } catch (Exception e) {
            pendingOp.updateStatus(TransactionStatus.FAILED, null, OffsetDateTime.now());
            krwtOperationRepository.save(pendingOp);
            log.error("Failed to burn KRWT on blockchain for user {}: {}", targetUserId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.BLOCKCHAIN_TRANSACTION_FAILED, "KRWT 소각 블록체인 트랜잭션 실패", e);
        }

        return KrwtBurnResponse.builder()
                .transactionId(pendingOp.getOperationId())
                .userId(targetUserId)
                .previousBalance(previousBalance)
                .burnAmount(request.getAmount())
                .newBalance(userAccount.getBalanceKrw())
                .transactionHash(txHash)
                .completedAt(pendingOp.getCompletedAt())
                .build();
    }

    @Transactional
    public void syncKrwtBalance(Long userId) {
        VirtualAccount account = virtualAccountRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "계정을 찾을 수 없습니다."));

        BigDecimal blockchainBalance = besuClient.getKrwtBalance(account.getWalletAddress());
        account.setBalanceKrw(blockchainBalance.longValue());
        virtualAccountRepository.save(account);
    }
}
