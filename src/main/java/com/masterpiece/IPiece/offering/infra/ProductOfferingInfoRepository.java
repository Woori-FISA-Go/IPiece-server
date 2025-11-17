package com.masterpiece.IPiece.offering.infra;

import com.masterpiece.IPiece.offering.domain.ProductOfferingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductOfferingInfoRepository extends JpaRepository<ProductOfferingInfo, Long> {
    //상품 id로 공모 정보 조회
    Optional<ProductOfferingInfo> findByProductId(Long productId);

    List<ProductOfferingInfo> findByProductIdIn(List<Long> productIds);
}
