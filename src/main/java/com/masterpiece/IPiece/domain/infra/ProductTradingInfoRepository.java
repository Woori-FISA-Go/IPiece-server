package com.masterpiece.IPiece.domain.infra;

import com.masterpiece.IPiece.domain.product.ProductTradingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTradingInfoRepository extends JpaRepository<ProductTradingInfo, Long> {
}
