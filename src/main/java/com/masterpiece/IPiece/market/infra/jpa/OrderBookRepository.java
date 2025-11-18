package com.masterpiece.IPiece.market.infra.jpa;

import com.masterpiece.IPiece.market.domain.OrderBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

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

    @Query("""
    SELECT o
      FROM OrderBook o
     WHERE o.product.productId = :productId
       AND o.orderType = com.masterpiece.IPiece.market.domain.OrderType.SELL
       AND o.pendingStatus = true
       AND o.orderPrice <= :buyPrice
     ORDER BY o.orderPrice ASC, o.createdAt ASC
    """)
    List<OrderBook> findMatchableSellOrders(Long productId, Long buyPrice);

    @Query("""
    SELECT o
      FROM OrderBook o
     WHERE o.product.productId = :productId
       AND o.orderType = com.masterpiece.IPiece.market.domain.OrderType.BUY
       AND o.pendingStatus = true
       AND o.orderPrice >= :sellPrice
     ORDER BY o.orderPrice DESC, o.createdAt ASC
    """)
    List<OrderBook> findMatchableBuyOrders(Long productId, Long sellPrice);
}