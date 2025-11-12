package com.masterpiece.IPiece.common.domain.infra;

import com.masterpiece.IPiece.common.domain.product.ProductTradingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTradingInfoRepository extends JpaRepository<ProductTradingInfo, Long> {
}
