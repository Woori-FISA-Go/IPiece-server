package com.masterpiece.IPiece.market.infra.adapter;

import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.market.application.port.ProductQueryPort;
import com.masterpiece.IPiece.market.infra.jpa.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductQueryAdapter implements ProductQueryPort {
    private final ProductJpaRepository repo;

    @Override
    public Page<Product> findActiveProducts(Pageable pageable) {
        // (이전) ACTIVE -> (변경) TRADE 기준으로 조회
        return repo.findTradeProducts(pageable);
    }
}