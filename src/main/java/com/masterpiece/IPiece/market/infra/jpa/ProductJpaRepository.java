package com.masterpiece.IPiece.market.infra.jpa;

import com.masterpiece.IPiece.common.domain.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Query("""
        SELECT p FROM Product p
        WHERE p.status = com.masterpiece.IPiece.common.domain.product.ProductStatus.TRADE
    """)
    Page<Product> findTradeProducts(Pageable pageable);
}
