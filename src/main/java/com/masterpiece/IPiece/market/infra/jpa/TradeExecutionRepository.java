package com.masterpiece.IPiece.market.infra.jpa;


import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.market.domain.TradeExecution;
import com.masterpiece.IPiece.market.infra.jpa.projection.PrevCloseProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeExecutionRepository extends JpaRepository<TradeExecution, Long> {

    @Query("""
        SELECT t
          FROM TradeExecution t
         WHERE t.product.productId = :productId
           AND t.matchTime BETWEEN :start AND :end
         ORDER BY t.matchTime ASC
    """)
    List<TradeExecution> findInWindow(
            @Param("productId") Long productId,
            @Param("start")     OffsetDateTime start,
            @Param("end")       OffsetDateTime end
    );

    @Query("""
        SELECT te
          FROM TradeExecution te
          JOIN te.buyOrder  bo
          JOIN te.sellOrder so
         WHERE te.matchTime BETWEEN :from AND :to
           AND (bo.virtualAccount = :account OR so.virtualAccount = :account)
        """)
    List<TradeExecution> findByAccountAndMatchTimeBetween(
            @Param("account") VirtualAccount account,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );

    @Query("""
            SELECT te.product.productId AS productId,
                   te.tradePrice        AS price
              FROM TradeExecution te
             WHERE te.product.productId IN :productIds
               AND te.matchTime >= :startAt
               AND te.matchTime <  :endAt
               AND te.matchTime = (
                   SELECT MAX(te2.matchTime)
                     FROM TradeExecution te2
                    WHERE te2.product.productId = te.product.productId
                      AND te2.matchTime >= :startAt
                      AND te2.matchTime <  :endAt
               )
             ORDER BY te.tradeId DESC
             LIMIT 1
            """)
    List<PrevCloseProjection> findAllPrevClosePrices(
            @Param("productIds") Collection<Long> productIds,
            @Param("startAt") OffsetDateTime startAt,
            @Param("endAt")   OffsetDateTime endAt);

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
         ORDER BY te.tradeId DESC
         LIMIT 1
        """)
    Long findPrevClosePrice(@Param("productId") Long productId,
                            @Param("startAt") OffsetDateTime startAt,
                            @Param("endAt")   OffsetDateTime endAt);

    @Query("""
    SELECT MAX(te.tradePrice)
      FROM TradeExecution te
     WHERE te.product.productId = :productId
       AND te.matchTime BETWEEN :from AND :to
    """)
    Optional<Long> findHighestPrice(@Param("productId") Long productId,
                          @Param("from") OffsetDateTime from,
                          @Param("to") OffsetDateTime to);

    @Query("""
    SELECT MIN(te.tradePrice)
      FROM TradeExecution te
     WHERE te.product.productId = :productId
       AND te.matchTime BETWEEN :from AND :to
    """)
    Optional<Long> findLowestPrice(@Param("productId") Long productId,
                         @Param("from") OffsetDateTime from,
                         @Param("to") OffsetDateTime to);

    @Query("""
    SELECT COALESCE(SUM(te.tradeQuantity), 0)
      FROM TradeExecution te
     WHERE te.product.productId = :productId
       AND te.matchTime BETWEEN :from AND :to
    """)
    Long findVolume(@Param("productId") Long productId,
                    @Param("from") OffsetDateTime from,
                    @Param("to") OffsetDateTime to);
}
