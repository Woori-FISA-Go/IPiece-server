package com.masterpiece.IPiece.common.domain.infra;

import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.product.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :productId")
    Optional<Product> findByIdWithLock(@Param("productId") Long productId);

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

    @Query(value = """
    SELECT p.*
    FROM product p
    JOIN product_offering_info o ON p.product_id = o.product_id
    WHERE p.product_id < COALESCE(:cursor, 9999999999)
    ORDER BY p.product_id DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Product> findProductsByOfferingInfo(
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );







}