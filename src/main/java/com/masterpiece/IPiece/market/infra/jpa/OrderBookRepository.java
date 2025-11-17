package com.masterpiece.IPiece.market.infra.jpa;

import com.masterpiece.IPiece.market.domain.OrderBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderBookRepository extends JpaRepository<OrderBook, Long> {

    Optional<OrderBook> findByIdempotencyKey(String idempotencyKey);

    @Query("""
        SELECT ob
          FROM OrderBook ob
          JOIN ob.virtualAccount va
          JOIN va.user u
         WHERE u.userId = :userId
           AND ob.product.productId = :productId
           AND ob.pendingStatus = true
         ORDER BY ob.clientTime DESC
    """)
    Page<OrderBook> findPendingOrders(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            Pageable pageable
    );
}