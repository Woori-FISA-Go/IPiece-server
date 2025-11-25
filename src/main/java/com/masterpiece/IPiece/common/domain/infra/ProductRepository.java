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

    boolean existsByProductNameIgnoreCase(String projectName);

    Page<Product> findByStatusAndProductNameContainingIgnoreCase(
            ProductStatus status, String projectName, Pageable pageable
    );

    @Query("""
        SELECT p FROM Product p
        WHERE p.status = com.masterpiece.IPiece.common.domain.product.ProductStatus.TRADE
    """)
    Page<Product> findTradeProducts(Pageable pageable);


    // 🔥 초기 로드 (cursor 없음)
    @Query(value = """
        SELECT *
        FROM product
        WHERE status = :status
        ORDER BY product_id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Product> findInitialPage(
            @Param("status") String status,
            @Param("limit") int limit
    );


    // 🔥 커서 기반 다음 페이지
    @Query(value = """
        SELECT *
        FROM product
        WHERE status = :status
          AND product_id <= :cursor
        ORDER BY product_id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Product> findNextPage(
            @Param("status") String status,
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );

    // 총 개수

    Long countProductsByStatus(ProductStatus productStatus);




}