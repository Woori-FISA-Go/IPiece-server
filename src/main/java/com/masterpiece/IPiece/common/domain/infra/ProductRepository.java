package com.masterpiece.IPiece.common.domain.infra;

import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    boolean existsByProjectNameIgnoreCase(String projectName);

    Page<Product> findByStatusAndProjectNameContainingIgnoreCase(
            ProductStatus status, String projectName, Pageable pageable
    );

    @Query("""
        SELECT p FROM Product p
        WHERE p.status = com.masterpiece.IPiece.common.domain.product.ProductStatus.TRADE
    """)
    Page<Product> findTradeProducts(Pageable pageable);

    
    // 무한 스크롤용 커서 기반 쿼리
    @Query("""
    SELECT p FROM Product p
    WHERE p.status = :status
    AND p.productId < :cursor
    ORDER BY p.productId DESC
    """)
    List<Product> findByStatusCursorBased(
            @Param("status") ProductStatus status,
            @Param("cursor") Long cursor
    );


    // 초기 로드 (cursor 없을 때)
    @Query("""
        SELECT p FROM Product p
        WHERE p.status = :status
        ORDER BY p.productId DESC
    """)
    List<Product> findByStatusInitial(
            @Param("status") ProductStatus status,
            @Param("limit") int limit
    );

}