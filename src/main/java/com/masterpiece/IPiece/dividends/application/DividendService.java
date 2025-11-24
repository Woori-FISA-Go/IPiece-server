package com.masterpiece.IPiece.dividends.application;

import com.masterpiece.IPiece.blockchain.domain.BlockchainTransaction;
import com.masterpiece.IPiece.blockchain.domain.TransactionStatus;
import com.masterpiece.IPiece.blockchain.domain.TransactionType;
import com.masterpiece.IPiece.blockchain.infra.jpa.BlockchainTransactionRepository;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.account.VirtualAccountJournal;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountJournalRepository;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.exception.BlockchainException;
import com.masterpiece.IPiece.dividends.domain.DividendPayouts;
import com.masterpiece.IPiece.dividends.domain.DividendStatus;
import com.masterpiece.IPiece.dividends.domain.Dividends;
import com.masterpiece.IPiece.dividends.infra.DividendPayoutsRepository;
import com.masterpiece.IPiece.dividends.infra.DividendsRepository;
import com.masterpiece.IPiece.integration.besu.BesuClient;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.mypage.infra.HoldingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 배당 배정/집행 서비스
 *
 * - allocateDueDividends():
 *   SCHEDULED & payoutDate <= now 인 Dividends 에 대해 DividendPayouts 생성
 * - executeDueDividends():
 *   PENDING Dividends 에 대해
 *   - BesuClient.transferKrwt 로 온체인 KRWT 전송
 *   - BlockchainTransaction 로그
 *   - VirtualAccount / VirtualAccountJournal / DividendPayouts / Dividends 상태 업데이트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DividendService {

    private final DividendsRepository dividendsRepository;
    private final DividendPayoutsRepository dividendPayoutsRepository;
    private final HoldingsRepository holdingsRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final VirtualAccountJournalRepository virtualAccountJournalRepository;
    private final BlockchainTransactionRepository blockchainTransactionRepository;
    private final BesuClient besuClient;

    /**
     * 배당 배정
     * @return 배정 처리된 Dividends 개수
     */
    @Transactional
    public int allocateDueDividends() {
        OffsetDateTime now = OffsetDateTime.now();

        List<Dividends> targets =
                dividendsRepository.findByStatusAndPayoutDateBefore(DividendStatus.SCHEDULED, now);

        if (targets.isEmpty()) {
            log.info("[BlockchainDividendService] allocateDueDividends - no SCHEDULED dividends to allocate");
            return 0;
        }

        int processed = 0;

        for (Dividends dividends : targets) {
            Product product = dividends.getProduct();
            if (product == null) {
                log.warn("[BlockchainDividendService] Dividends {} has no product, skip allocation",
                        dividends.getDividendId());
                continue;
            }

            Long totalAmount = dividends.getTotalAmount();
            Long totalTokenQuantity = product.getTokenQuantity();
            if (totalAmount == null || totalAmount <= 0 ||
                    totalTokenQuantity == null || totalTokenQuantity <= 0) {
                log.warn("[BlockchainDividendService] Dividends {} invalid totalAmount/tokenQuantity, skip",
                        dividends.getDividendId());
                continue;
            }

            List<Holdings> holdingsList = holdingsRepository.findAllByProduct(product);
            if (holdingsList.isEmpty()) {
                log.info("[BlockchainDividendService] No holdings for product {} in dividends {}",
                        product.getProductId(), dividends.getDividendId());
                dividends.markAllocated(0L, totalAmount, 0);
                processed++;
                continue;
            }

            long amountPerToken = totalAmount / totalTokenQuantity;
            if (amountPerToken <= 0) {
                log.warn("[BlockchainDividendService] Dividends {} amountPerToken=0, skip",
                        dividends.getDividendId());
                continue;
            }

            long distributedSum = 0L;
            int recipientCount = 0;

            for (Holdings h : holdingsList) {
                Long quantity = h.getQuantity();
                if (quantity == null || quantity <= 0) {
                    continue;
                }

                VirtualAccount va = h.getVirtualAccount();
                if (va == null) {
                    log.warn("[BlockchainDividendService] Holdings {} has no VirtualAccount, skip",
                            h.getHoldingId());
                    continue;
                }

                long payoutAmount = quantity * amountPerToken;
                if (payoutAmount <= 0) {
                    continue;
                }

                DividendPayouts payout = DividendPayouts.builder()
                        .dividends(dividends)
                        .virtualAccount(va)
                        .payoutAmount(payoutAmount)
                        .payoutStatus("PENDING")
                        .payoutDate(now)
                        .build();

                dividendPayoutsRepository.save(payout);

                distributedSum += payoutAmount;
                recipientCount++;
            }

            long remainder = totalAmount - distributedSum;
            if (remainder < 0) remainder = 0;

            dividends.markAllocated(distributedSum, remainder, recipientCount);
            processed++;
        }

        log.info("[BlockchainDividendService] allocateDueDividends - processed={} dividends", processed);
        return processed;
    }

    /**
     * 배당 집행 (온체인 KRWT 전송 + 오프체인 반영)
     * @return COMPLETED 로 전환된 Dividends 개수
     */
    @Transactional
    public int executeDueDividends() {
        OffsetDateTime now = OffsetDateTime.now();

        List<Dividends> targets = dividendsRepository.findByStatus(DividendStatus.PENDING);
        if (targets.isEmpty()) {
            log.info("[BlockchainDividendService] executeDueDividends - no PENDING dividends");
            return 0;
        }

        // admin 지갑 주소 한 번만 조회해서 재사용
        String adminAddress = besuClient.getAdminAddress();

        int completedCount = 0;

        for (Dividends dividends : targets) {
            List<DividendPayouts> payouts =
                    dividendPayoutsRepository.findByDividendsAndPayoutStatus(dividends, "PENDING");

            if (payouts.isEmpty()) {
                dividends.markCompleted();
                completedCount++;
                continue;
            }

            boolean allSuccess = true;

            for (DividendPayouts payout : payouts) {
                VirtualAccount account = payout.getVirtualAccount();
                if (account == null) {
                    log.warn("[BlockchainDividendService] payout {} has no VirtualAccount, mark FAILED",
                            payout.getPayoutId());
                    payout.markFailed(now);
                    allSuccess = false;
                    continue;
                }

                Long amount = payout.getPayoutAmount();
                if (amount == null || amount <= 0) {
                    log.warn("[BlockchainDividendService] payout {} invalid amount {}, mark FAILED",
                            payout.getPayoutId(), amount);
                    payout.markFailed(now);
                    allSuccess = false;
                    continue;
                }

                String walletAddress = account.getWalletAddress();
                if (walletAddress == null || walletAddress.isBlank()) {
                    log.warn("[BlockchainDividendService] account {} has no walletAddress, payout {} FAILED",
                            account.getAccountId(), payout.getPayoutId());
                    payout.markFailed(now);
                    allSuccess = false;
                    continue;
                }

                BlockchainTransaction txLog = null;

                try {
                    // 1) 온체인 KRWT 전송 (admin → user 지갑)
                    String txHash = besuClient.transferKrwt(walletAddress, amount);

                    // 2) BlockchainTransaction 로그 (SUCCESS)
                    txLog = BlockchainTransaction.builder()
                            .transactionType(TransactionType.DIVIDEND)
                            .txHash(txHash)
                            .fromAddress(adminAddress)
                            .toAddress(walletAddress)
                            .amount(BigDecimal.valueOf(amount))
                            .tokenAddress(null) // KRWT 컨트랙트 주소 넣고 싶으면 주입
                            .blockNumber(null) // 필요하면 receipt 조회 추가
                            .blockHash(null)
                            .gasUsed(null)
                            .transactionStatus(TransactionStatus.SUCCESS)
                            .user(account.getUser())
                            .build();
                    blockchainTransactionRepository.save(txLog);

                    // 3) 가상계좌 잔고 증가
                    long beforeBalance = account.getBalanceKrw();
                    account.increaseBalanceKrw(amount);
                    virtualAccountRepository.save(account);

                    // 4) 분개장 기록
                    VirtualAccountJournal journal = VirtualAccountJournal.builder()
                            .virtualAccount(account)
                            .txType("DIVIDEND")
                            .amountKrw(amount)
                            .balanceAfter(account.getBalanceKrw())
                            .description(buildDividendDescription(dividends))
                            .build();
                    virtualAccountJournalRepository.save(journal);

                    // 5) payout 상태 갱신
                    payout.markPaid(now);

                    log.info("[BlockchainDividendService] payout {} executed: accountId={} wallet={} amount={} before={} after={} txHash={}",
                            payout.getPayoutId(),
                            account.getAccountId(),
                            walletAddress,
                            amount,
                            beforeBalance,
                            account.getBalanceKrw(),
                            txHash);

                } catch (Exception ex) {
                    log.error("[BlockchainDividendService] payout {} execution FAILED: {}",
                            payout.getPayoutId(), ex.getMessage(), ex);

                    payout.markFailed(now);
                    allSuccess = false;

                    // BlockchainTransaction 로그 (FAILED)
                    if (txLog == null) {
                        BlockchainTransaction failedTx = BlockchainTransaction.builder()
                                .transactionType(TransactionType.DIVIDEND)
                                .txHash(null)
                                .fromAddress(adminAddress)
                                .toAddress(walletAddress)
                                .amount(BigDecimal.valueOf(amount))
                                .tokenAddress(null)
                                .blockNumber(null)
                                .blockHash(null)
                                .gasUsed(null)
                                .transactionStatus(TransactionStatus.FAILED)
                                .errorMessage(ex.getMessage())
                                .user(account.getUser())
                                .build();
                        blockchainTransactionRepository.save(failedTx);
                    } else {
                        txLog.recordError(ex.getMessage());
                        blockchainTransactionRepository.save(txLog);
                    }
                }
            }

            if (allSuccess) {
                dividends.markCompleted();
                completedCount++;
            } else {
                dividends.markFailed();
            }
        }

        log.info("[BlockchainDividendService] executeDueDividends - completed={} dividends", completedCount);
        return completedCount;
    }

    private String buildDividendDescription(Dividends dividends) {
        Product product = dividends.getProduct();
        String productName = (product != null ? product.getProductName() : "Unknown");
        return productName + " 배당금 지급";
    }
}