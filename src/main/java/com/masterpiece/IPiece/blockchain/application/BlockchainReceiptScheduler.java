package com.masterpiece.IPiece.blockchain.application;

import com.masterpiece.IPiece.blockchain.domain.BlockchainTransaction;
import com.masterpiece.IPiece.blockchain.domain.TransactionStatus;
import com.masterpiece.IPiece.blockchain.infra.jpa.BlockchainTransactionRepository;
import com.masterpiece.IPiece.integration.besu.BesuClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = true)
public class BlockchainReceiptScheduler {

    private final BlockchainTransactionRepository txRepository;
    private final BesuClient besuClient;

    /**
     * 주기적으로 PENDING 트랜잭션의 영수증을 조회해 SUCCESS/FAILED로 확정한다.
     * 프론트/API 스펙 변경 없이 상태만 업데이트한다.
     */
    @Scheduled(fixedDelayString = "${blockchain.receipt.poll-interval-ms:10000}")
    @Transactional
    public void pollReceipts() {
        List<BlockchainTransaction> pendingTxs =
                txRepository.findTop100ByTransactionStatusOrderByCreatedAtAsc(TransactionStatus.PENDING);

        if (pendingTxs.isEmpty()) {
            return;
        }

        for (BlockchainTransaction tx : pendingTxs) {
            try {
                Optional<TransactionReceipt> receiptOpt = besuClient.fetchTransactionReceipt(tx.getTxHash());
                if (receiptOpt.isEmpty()) {
                    // 아직 채굴 안 됨
                    continue;
                }

                TransactionReceipt receipt = receiptOpt.get();
                Long blockNumber = receipt.getBlockNumber() != null ? receipt.getBlockNumber().longValue() : null;
                String blockHash = receipt.getBlockHash();
                Long gasUsed = receipt.getGasUsed() != null ? receipt.getGasUsed().longValue() : null;

                boolean success = "0x1".equalsIgnoreCase(receipt.getStatus());
                if (success) {
                    tx.markSuccess(blockNumber, blockHash, gasUsed);
                } else {
                    tx.markFailed("Receipt status != 0x1", blockNumber, blockHash, gasUsed);
                }
            } catch (Exception e) {
                log.warn("Failed to update receipt for tx {}: {}", tx.getTxHash(), e.getMessage());
                tx.markFailed(e.getMessage(), null, null, null);
            }
        }
    }
}
