package com.masterpiece.IPiece.market.infra.jpa;

import com.masterpiece.IPiece.common.domain.product.ProductTradingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductTradingInfoJpaRepository extends JpaRepository<ProductTradingInfo, Long> {
}
