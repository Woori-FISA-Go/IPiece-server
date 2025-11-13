package com.masterpiece.IPiece.market.infra.jpa;


import com.masterpiece.IPiece.market.domain.TradeExecution;
import com.masterpiece.IPiece.market.infra.jpa.projection.PrevCloseProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TradeExecutionRepository extends JpaRepository<TradeExecution, Long> {
    @Query(value = """
        SELECT DISTINCT ON (product_id) 
               product_id,
               trade_price AS price
          FROM trade_execution
         WHERE product_id IN (:productIds)
           AND created_at >= :startAt
           AND created_at <  :endAt
         ORDER BY product_id, created_at DESC
        """, nativeQuery = true)
    List<PrevCloseProjection> findAllPrevClosePrices(
            @Param("productIds") Collection<Long> productIds,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt")   OffsetDateTime endAt);

    @Query(value = """
        SELECT trade_price
          FROM trade_execution
         WHERE product_id = :productId
           AND matchtime >= :startAt
           AND matchtime <  :endAt
         ORDER BY matchtime DESC
         LIMIT 1
        """, nativeQuery = true)
    Long findPrevClosePrice(@Param("productId") Long productId,
                            @Param("startAt") OffsetDateTime startAt,
                            @Param("endAt")   OffsetDateTime endAt);
}