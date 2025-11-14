package com.masterpiece.IPiece.market.infra.jpa;


import com.masterpiece.IPiece.market.domain.TradeExecution;
import com.masterpiece.IPiece.market.infra.jpa.projection.PrevCloseProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TradeExecutionRepository extends JpaRepository<TradeExecution, Long> {

    @Query("""
            SELECT te.product.productId AS productId,
                   te.tradePrice        AS price
              FROM TradeExecution te
             WHERE te.product.productId IN :productIds
               AND te.matchTime >= :startAt
               AND te.matchTime <  :endAt
               AND te.matchTime = (
                   SELECT MAX(te2.createAt)
                     FROM TradeExecution te2
                    WHERE te2.product.productId = te.product.productId
                      AND te2.createAt >= :startAt
                      AND te2.createAt <  :endAt
               )
            """)
    List<PrevCloseProjection> findAllPrevClosePrices(
            @Param("productIds") Collection<Long> productIds,
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt")   LocalDateTime endAt);

    @Query("""
        SELECT te.tradePrice
          FROM TradeExecution te
         WHERE te.product.productId = :productId
           AND te.matchTime >= :startAt
           AND te.matchTime <  :endAt
           AND te.matchTime = (
               SELECT MAX(te2.matchTime)
                 FROM TradeExecution te2
                WHERE te2.product.productId = :productId
                  AND te2.matchTime >= :startAt
                  AND te2.matchTime <  :endAt
           )
        """)
    Long findPrevClosePrice(@Param("productId") Long productId,
                            @Param("startAt") LocalDateTime startAt,
                            @Param("endAt")   LocalDateTime endAt);
}
