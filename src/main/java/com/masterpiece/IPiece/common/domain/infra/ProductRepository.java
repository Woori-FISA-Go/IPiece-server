package com.masterpiece.IPiece.common.domain.infra;

import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    boolean existsByProjectNameIgnoreCase(String projectName);

    Page<Product> findByStatusAndProjectNameContainingIgnoreCase(
            ProductStatus status, String projectName, Pageable pageable
    );
}