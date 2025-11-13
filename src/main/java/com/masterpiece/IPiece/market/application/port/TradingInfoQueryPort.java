package com.masterpiece.IPiece.market.application.port;

import com.masterpiece.IPiece.common.domain.product.Disclosure;
import com.masterpiece.IPiece.common.domain.product.ProductTradingInfo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface TradingInfoQueryPort {
    Optional<ProductTradingInfo> findTradingInfo(Long productId);

    List<Disclosure> findDisclosures(Long productId, int limit);
}
