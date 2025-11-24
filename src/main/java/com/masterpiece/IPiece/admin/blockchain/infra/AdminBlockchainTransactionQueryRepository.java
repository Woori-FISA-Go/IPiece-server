package com.masterpiece.IPiece.admin.blockchain.infra;

import com.masterpiece.IPiece.blockchain.domain.BlockchainTransaction;
import com.masterpiece.IPiece.blockchain.domain.TransactionStatus;
import com.masterpiece.IPiece.blockchain.domain.TransactionType;
import com.masterpiece.IPiece.common.domain.product.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminBlockchainTransactionQueryRepository {

    private final EntityManager em;

    /**
     * 온체인 트랜잭션 검색
     * - userId, txType, status, productId (product.tokenContractAddress 기준) 필터
     * - createdAt DESC 정렬
     */
    public Page<BlockchainTransaction> search(
            Long userId,
            TransactionType txType,
            TransactionStatus status,
            Long productId,
            Pageable pageable
    ) {
        StringBuilder jpql = new StringBuilder(
                "SELECT t FROM BlockchainTransaction t " +
                        "LEFT JOIN " + Product.class.getName() + " p " +
                        "ON t.tokenAddress = p.tokenContractAddress WHERE 1=1"
        );

        StringBuilder countJpql = new StringBuilder(
                "SELECT COUNT(t) FROM BlockchainTransaction t " +
                        "LEFT JOIN " + Product.class.getName() + " p " +
                        "ON t.tokenAddress = p.tokenContractAddress WHERE 1=1"
        );

        List<String> conditions = new ArrayList<>();
        if (userId != null) {
            conditions.add("t.user.userId = :userId");
        }
        if (txType != null) {
            conditions.add("t.transactionType = :txType");
        }
        if (status != null) {
            conditions.add("t.transactionStatus = :status");
        }
        if (productId != null) {
            conditions.add("p.productId = :productId");
        }

        for (String cond : conditions) {
            jpql.append(" AND ").append(cond);
            countJpql.append(" AND ").append(cond);
        }

        jpql.append(" ORDER BY t.createdAt DESC");

        TypedQuery<BlockchainTransaction> query =
                em.createQuery(jpql.toString(), BlockchainTransaction.class);
        TypedQuery<Long> countQuery =
                em.createQuery(countJpql.toString(), Long.class);

        if (userId != null) {
            query.setParameter("userId", userId);
            countQuery.setParameter("userId", userId);
        }
        if (txType != null) {
            query.setParameter("txType", txType);
            countQuery.setParameter("txType", txType);
        }
        if (status != null) {
            query.setParameter("status", status);
            countQuery.setParameter("status", status);
        }
        if (productId != null) {
            query.setParameter("productId", productId);
            countQuery.setParameter("productId", productId);
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<BlockchainTransaction> content = query.getResultList();
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}