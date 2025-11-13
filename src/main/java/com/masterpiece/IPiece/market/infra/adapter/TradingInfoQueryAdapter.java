package com.masterpiece.IPiece.market.infra.adapter;

import com.masterpiece.IPiece.common.domain.product.Disclosure;
import com.masterpiece.IPiece.common.domain.product.ProductTradingInfo;
import com.masterpiece.IPiece.market.application.port.TradingInfoQueryPort;
import com.masterpiece.IPiece.market.infra.jpa.DisclosureRepository;
import com.masterpiece.IPiece.market.infra.jpa.ProductTradingInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TradingInfoQueryAdapter implements TradingInfoQueryPort {

    private final ProductTradingInfoRepository productTradingInfoRepository;
    private final DisclosureRepository disclosureRepository;

    @Override
    public Optional<ProductTradingInfo> findTradingInfo(Long productId) {
        return productTradingInfoRepository.findById(productId);
    }

    @Override
    public List<Disclosure> findDisclosures(Long productId, int limit) {
        return disclosureRepository.findByProductId(
                productId,
                PageRequest.of(0, Math.max(limit, 1))
        );
    }
}