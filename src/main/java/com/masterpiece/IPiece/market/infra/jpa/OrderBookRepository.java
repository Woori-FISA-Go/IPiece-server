package com.masterpiece.IPiece.market.infra.jpa;

import com.masterpiece.IPiece.market.domain.OrderBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBookRepository extends JpaRepository<OrderBook, Long> {

    @Query("""
        SELECT ob
          FROM OrderBook ob
          JOIN ob.virtualAccount va
          JOIN va.user u
         WHERE u.userId = :userId
           AND ob.product.productId = :productId
           AND ob.pendingStatus = true
         ORDER BY ob.createTime DESC
    """)
    Page<OrderBook> findPendingOrders(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            Pageable pageable
    );
}