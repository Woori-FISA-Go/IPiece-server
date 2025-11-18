package com.masterpiece.IPiece.offering.infra;

import com.masterpiece.IPiece.offering.domain.OfferingStatus;
import com.masterpiece.IPiece.offering.domain.OfferingSubscriptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfferingSubscriptionsRepository extends JpaRepository<OfferingSubscriptions, Long> {

    @Query("""
        SELECT COALESCE(SUM(s.appliedQuantity), 0)
        FROM OfferingSubscriptions s
        WHERE s.productOfferingInfo.productId = :productId
    """)
    Long sumAppliedQuantityByProductId(@Param("productId") Long productId);
}
