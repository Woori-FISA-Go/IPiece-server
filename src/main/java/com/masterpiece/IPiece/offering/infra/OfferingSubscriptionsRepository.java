package com.masterpiece.IPiece.offering.infra;

import com.masterpiece.IPiece.offering.domain.OfferingStatus;
import com.masterpiece.IPiece.offering.domain.OfferingSubscriptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferingSubscriptionsRepository extends JpaRepository<OfferingSubscriptions, Long> {

    Page<OfferingSubscriptions> findByProductId(Long productId, Pageable pageable);

    Page<OfferingSubscriptions> findByProductIdAndStatus(Long productId, OfferingStatus status, Pageable pageable);

    boolean existsByProductIdAndAccountId(Long productId, String accountId);
}
