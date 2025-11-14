package com.masterpiece.IPiece.market.infra.adapter;

import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.market.application.port.ProductQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductQueryAdapter implements ProductQueryPort {
    private final ProductRepository productRepository;

    @Override
    public Page<Product> findTradeProducts(Pageable pageable) {
        return productRepository.findTradeProducts(pageable);
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return productRepository.findById(productId);
    }
}