package com.masterpiece.IPiece.offering.infra;

import com.masterpiece.IPiece.offering.domain.OfferingStatus;
import com.masterpiece.IPiece.offering.domain.OfferingSubscriptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferingSubscriptionsRepository extends JpaRepository<OfferingSubscriptions, Long> {

    Page<OfferingSubscriptions> findByProductOfferingInfo_ProductId(Long productId, Pageable pageable);

    Page<OfferingSubscriptions> findByProductOfferingInfo_ProductIdAndStatus(Long productId, OfferingStatus status, Pageable pageable);

    boolean existsByProductOfferingInfo_ProductIdAndVirtualAccount_AccountId(Long productId, Long accountId);
}
