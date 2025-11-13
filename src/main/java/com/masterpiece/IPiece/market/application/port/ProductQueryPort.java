package com.masterpiece.IPiece.market.application.port;

import com.masterpiece.IPiece.common.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductQueryPort {
    Page<Product> findActiveProducts(Pageable pageable);

    Optional<Product> findById(Long productId);
}
