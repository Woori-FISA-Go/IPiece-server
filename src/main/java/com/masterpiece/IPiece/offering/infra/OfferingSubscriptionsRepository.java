package com.masterpiece.IPiece.offering.infra;

import com.masterpiece.IPiece.mypage.api.dto.response.OfferingAssetDto;
import com.masterpiece.IPiece.offering.domain.OfferingSubscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OfferingSubscriptionsRepository extends JpaRepository<OfferingSubscriptions, Long> {

    @Query("""
        SELECT COALESCE(SUM(s.appliedQuantity), 0)
        FROM OfferingSubscriptions s
        WHERE s.productOfferingInfo.productId = :productId
    """)
    Long sumAppliedQuantityByProductId(@Param("productId") Long productId);

    @Query("""
    SELECT new com.masterpiece.IPiece.mypage.api.dto.response.OfferingAssetDto(
        p.productId,
        p.productName,
        p.tokenName,
        p.thumbnailImg,
        SUM(os.appliedQuantity),
        poi.offeringPrice,
        SUM(os.appliedAmountKrw),
        poi.progressRate,
        poi.offeringStartDate,
        poi.offeringEndDate
    )
    FROM OfferingSubscriptions os
    JOIN os.virtualAccount va
    JOIN os.productOfferingInfo poi
    JOIN poi.product p
    WHERE va.accountId = :accountId
    GROUP BY 
        p.productId,
        p.productName,
        p.tokenName,
        p.thumbnailImg,
        poi.offeringPrice,
        poi.progressRate,
        poi.offeringStartDate,
        poi.offeringEndDate
    ORDER BY p.productId DESC
""")
    List<OfferingAssetDto> findOfferingAssetsByAccountId(@Param("accountId") Long accountId);

}
