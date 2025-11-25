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
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.mypage.infra.HoldingsRepository;
import com.masterpiece.IPiece.integration.besu.BesuClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final VirtualAccountRepository virtualAccountRepository;
    private final HoldingsRepository holdingsRepository;
    private final KrwtOperationRepository krwtOperationRepository; // Inject KrwtOperationRepository
    private final BesuClient besuClient;

    private static final long MAX_MINT_AMOUNT = 100_000_000L; // 1억원

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
                            .tokenAddress(null) // Assuming token address is not available
                            .symbol(holding.getProduct().getTokenName())
                            .balance(holding.getQuantity())
                            .totalSupply(holding.getProduct().getTotalTokenQuantity())
                            .sharePercentage(sharePercentage.toPlainString() + "%")
                            .totalDividendsReceived(0L) // Assuming total dividends received is not available
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
        VirtualAccount virtualAccount = virtualAccountRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User " + userId + " has no virtual account"));

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("createdAt").descending());

        Page<KrwtOperation> operationsPage;

        // Filter by type
        if (!"ALL".equalsIgnoreCase(request.getType())) {
            OperationType operationType = OperationType.valueOf(request.getType().toUpperCase());
            operationsPage = krwtOperationRepository.findByUserUserIdAndOperationType(userId, operationType, pageable);
        } else {
            operationsPage = krwtOperationRepository.findByUserUserId(userId, pageable);
        }

        // TODO: Add date filtering if needed. KrwtOperationRepository needs custom query for date range.

        List<TransactionListResponse.TransactionItem> transactionItems = operationsPage.getContent().stream()
                .map(op -> TransactionListResponse.TransactionItem.builder()
                        .journalId(op.getOperationId())
                        .type(op.getOperationType().name())
                        .amount(op.getAmount().longValueExact()) // Convert BigDecimal to Long
                        .balanceAfter(op.getAfterBalance().longValueExact()) // Convert BigDecimal to Long
                        .memo(op.getMemo())
                        .transactionHash(op.getTxHash())
                        .createdAt(op.getCreatedAt()) // Corrected getter
                        .build())
                .collect(Collectors.toList());

        return TransactionListResponse.builder()
                .totalElements(operationsPage.getTotalElements())
                .totalPages(operationsPage.getTotalPages())
                .currentPage(operationsPage.getNumber())
                .pageSize(operationsPage.getSize())
                .transactions(transactionItems)
                .build();
    }

    @Transactional
    public KrwtMintResponse mintKrwt(Long adminUserId, KrwtMintRequest request) {
        // 1. 관리자 권한 검증 (컨트롤러에서 @PreAuthorize로 처리)
        // 2. amount 유효성 검증
        if (request.getAmount() <= 0 || request.getAmount() > MAX_MINT_AMOUNT) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "발행 금액은 0보다 크고 " + MAX_MINT_AMOUNT + "원 이하여야 합니다.");
        }

        // 3. 입금 받을 사용자 VirtualAccount 조회
        VirtualAccount targetAccount = virtualAccountRepository.findByUser_UserId(request.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "입금 받을 사용자를 찾을 수 없습니다."));

        // 4. 이전 잔고 기록
        long previousBalance = targetAccount.getBalanceKrw();

        // ✅ 1. PENDING 상태로 먼저 KrwtOperation 기록
        KrwtOperation pendingOp = KrwtOperation.builder()
                .user(targetAccount.getUser())
                .virtualAccount(targetAccount)
                .operationType(OperationType.MINT)
                .amount(BigDecimal.valueOf(request.getAmount()))
                .beforeBalance(BigDecimal.valueOf(previousBalance))
                .afterBalance(BigDecimal.valueOf(previousBalance + request.getAmount())) // 예상되는 최종 잔고
                .status(TransactionStatus.PENDING) // PENDING 상태로 시작
                .memo(request.getMemo())
                .createdAt(OffsetDateTime.now()) // 생성 시간 기록
                .build();
        krwtOperationRepository.save(pendingOp);

        String txHash;
        try {
            // ✅ 2. 블록체인 호출 수행
            txHash = besuClient.mintKrwt(targetAccount.getWalletAddress(), BigInteger.valueOf(request.getAmount()));

            // ✅ 3. 블록체인 성공 후 DB 업데이트
            targetAccount.increaseBalanceKrw(request.getAmount());
            virtualAccountRepository.save(targetAccount);

            // ✅ 4. KrwtOperation 상태를 SUCCESS로 업데이트
            pendingOp.updateStatus(TransactionStatus.SUCCESS, txHash, OffsetDateTime.now());
            krwtOperationRepository.save(pendingOp);

        } catch (Exception e) {
            // ✅ 5. 블록체인 호출 실패 시 KrwtOperation 상태를 FAILED로 업데이트
            pendingOp.updateStatus(TransactionStatus.FAILED, null, OffsetDateTime.now()); // txHash는 null
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
        // 1. amount 유효성 검증
        if (request.getAmount() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT, "출금 금액은 0보다 커야 합니다.");
        }

        // 2. 대상 사용자 VirtualAccount 조회
        VirtualAccount userAccount = virtualAccountRepository.findByUser_UserId(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자 지갑을 찾을 수 없습니다."));

        // 3. 잔고 확인
        if (userAccount.getBalanceKrw() < request.getAmount()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE, "잔고가 부족합니다.");
        }

        // 4. 이전 잔고 기록
        long previousBalance = userAccount.getBalanceKrw();

        // ✅ 1. PENDING 상태로 먼저 KrwtOperation 기록
        KrwtOperation pendingOp = KrwtOperation.builder()
                .user(userAccount.getUser())
                .virtualAccount(userAccount)
                .operationType(OperationType.BURN)
                .amount(BigDecimal.valueOf(request.getAmount()))
                .beforeBalance(BigDecimal.valueOf(previousBalance))
                .afterBalance(BigDecimal.valueOf(previousBalance - request.getAmount())) // 예상되는 최종 잔고
                .status(TransactionStatus.PENDING) // PENDING 상태로 시작
                .memo(request.getMemo())
                .createdAt(OffsetDateTime.now()) // 생성 시간 기록
                .build();
        krwtOperationRepository.save(pendingOp);

        String txHash;
        try {
            // ✅ 2. 블록체인 호출 수행
            txHash = besuClient.burnKrwt(userAccount.getWalletAddress(), BigInteger.valueOf(request.getAmount()));

            // ✅ 3. 블록체인 성공 후 DB 업데이트
            userAccount.decreaseBalanceKrw(request.getAmount());
            virtualAccountRepository.save(userAccount);

            // ✅ 4. KrwtOperation 상태를 SUCCESS로 업데이트
            pendingOp.updateStatus(TransactionStatus.SUCCESS, txHash, OffsetDateTime.now());
            krwtOperationRepository.save(pendingOp);

        } catch (Exception e) {
            // ✅ 5. 블록체인 호출 실패 시 KrwtOperation 상태를 FAILED로 업데이트
            pendingOp.updateStatus(TransactionStatus.FAILED, null, OffsetDateTime.now()); // txHash는 null
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
}
