package com.masterpiece.IPiece.market.infra.jpa;

import com.masterpiece.IPiece.common.domain.product.Disclosure;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisclosureJpaRepository extends JpaRepository<Disclosure, Long> {

    @Query("""
        SELECT d
          FROM Disclosure d
         WHERE d.productTradingInfo.productId = :productId
         ORDER BY d.disclosureDate DESC
        """)
    List<Disclosure> findByProductId(@Param("productId") Long productId, Pageable pageable);
}