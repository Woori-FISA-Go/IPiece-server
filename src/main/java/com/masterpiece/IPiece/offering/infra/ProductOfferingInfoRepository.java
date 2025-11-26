package com.masterpiece.IPiece.offering.infra;

import com.masterpiece.IPiece.offering.domain.ProductOfferingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductOfferingInfoRepository extends JpaRepository<ProductOfferingInfo, Long> {
    //상품 id로 공모 정보 조회
    Optional<ProductOfferingInfo> findByProductId(Long productId);

    List<ProductOfferingInfo> findByProductIdIn(List<Long> productIds);

    @Query("""
SELECT o
FROM ProductOfferingInfo o
JOIN o.product p
""")
    List<ProductOfferingInfo> findAllOfferingProducts();


    @Query("""
SELECT COUNT(o)
FROM ProductOfferingInfo o
JOIN o.product p
WHERE o.offeringStartDate > :now
  AND o.progressRate != 100
""")
    Long countUpcoming(@Param("now") OffsetDateTime now);


    @Query("""
SELECT COUNT(o)
FROM ProductOfferingInfo o
JOIN o.product p
WHERE o.offeringStartDate <= :now
  AND o.offeringEndDate >= :now
  AND o.progressRate != 100
""")
    Long countOngoing(@Param("now") OffsetDateTime now);


    @Query("""
SELECT COUNT(DISTINCT o.product.productId)
FROM ProductOfferingInfo o
JOIN o.product p
WHERE (o.offeringEndDate < :now OR o.progressRate = 100)
   OR p.status = 'TRADE'

""")
    Long countClosedOrSoldOut(@Param("now") OffsetDateTime now);




}
