package com.masterpiece.IPiece.market.application.port;

import com.masterpiece.IPiece.common.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQueryPort {
    Page<Product> findActiveProducts(Pageable pageable);
}
