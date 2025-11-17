package com.masterpiece.IPiece.blockchain.infra.jpa;

import com.masterpiece.IPiece.blockchain.domain.KrwtOperation;
import com.masterpiece.IPiece.blockchain.domain.OperationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KrwtOperationRepository extends JpaRepository<KrwtOperation, Long> {
    Page<KrwtOperation> findByUserUserId(Long userId, Pageable pageable);
    List<KrwtOperation> findByOperationType(OperationType operationType);
    List<KrwtOperation> findByVirtualAccountAccountId(Long accountId);
}
