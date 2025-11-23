package com.masterpiece.IPiece.admin.blockchain.application;

import com.masterpiece.IPiece.admin.blockchain.api.dto.response.AdminBlockchainTransactionListResponse;
import com.masterpiece.IPiece.admin.blockchain.api.dto.response.AdminBlockchainTransactionListResponse.Item;
import com.masterpiece.IPiece.admin.blockchain.infra.AdminBlockchainTransactionQueryRepository;
import com.masterpiece.IPiece.blockchain.domain.BlockchainTransaction;
import com.masterpiece.IPiece.blockchain.domain.TransactionStatus;
import com.masterpiece.IPiece.blockchain.domain.TransactionType;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminBlockchainTransactionService {

    private final AdminBlockchainTransactionQueryRepository queryRepository;

    @Transactional(readOnly = true)
    public AdminBlockchainTransactionListResponse searchTransactions(
            Long userId,
            String txTypeStr,
            String statusStr,
            Long productId,
            Pageable pageable
    ) {
        TransactionType txType = null;
        if (txTypeStr != null && !txTypeStr.isBlank()) {
            try {
                txType = TransactionType.valueOf(txTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "tx_type 값이 올바르지 않습니다.");
            }
        }

        TransactionStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = TransactionStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "status 값이 올바르지 않습니다.");
            }
        }

        Page<BlockchainTransaction> pageResult =
                queryRepository.search(userId, txType, status, productId, pageable);

        List<Item> items = pageResult.getContent().stream()
                .map(this::toItem)
                .toList();

        return AdminBlockchainTransactionListResponse.builder()
                .items(items)
                .page(pageable.getPageNumber() + 1)
                .pageSize(pageable.getPageSize())
                .totalCount(pageResult.getTotalElements())
                .build();
    }

    private Item toItem(BlockchainTransaction tx) {
        return Item.builder()
                // DB: tx_id
                .transactionId(tx.getTxId())
                .txHash(tx.getTxHash())
                .transactionStatus(tx.getTransactionStatus().name())
                .transactionType(tx.getTransactionType().name())
                // DB: user_id (ManyToOne User user)
                .userId(tx.getUser() != null ? tx.getUser().getUserId() : null)
                .fromAddress(tx.getFromAddress())
                .toAddress(tx.getToAddress())
                // DB: token_address
                .contractAddress(tx.getTokenAddress())
                .amount(tx.getAmount())
                .blockNumber(tx.getBlockNumber())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}