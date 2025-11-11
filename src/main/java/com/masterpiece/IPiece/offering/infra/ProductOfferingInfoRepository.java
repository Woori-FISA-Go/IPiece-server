package com.masterpiece.IPiece.offering.infra;

import com.masterpiece.IPiece.offering.domain.ProductOfferingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOfferingInfoRepository extends JpaRepository<ProductOfferingInfo, Long> {
    boolean existsByProductId(Long productId);
}
