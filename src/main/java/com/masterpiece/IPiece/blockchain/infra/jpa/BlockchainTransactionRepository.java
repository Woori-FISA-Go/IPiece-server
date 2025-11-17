package com.masterpiece.IPiece.blockchain.infra.jpa;

import com.masterpiece.IPiece.blockchain.domain.BlockchainTransaction;
import com.masterpiece.IPiece.blockchain.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockchainTransactionRepository extends JpaRepository<BlockchainTransaction, Long> {
    Optional<BlockchainTransaction> findByTxHash(String txHash);
    Page<BlockchainTransaction> findByUserUserId(Long userId, Pageable pageable);
    Page<BlockchainTransaction> findByTxType(TransactionType txType, Pageable pageable);
}
