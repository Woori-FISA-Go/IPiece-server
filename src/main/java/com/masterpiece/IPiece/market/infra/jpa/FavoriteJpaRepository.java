package com.masterpiece.IPiece.market.infra.jpa;

import com.masterpiece.IPiece.common.domain.product.Product;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface FavoriteJpaRepository extends JpaRepository<Product, Long> {

    @Query(value = """
        SELECT fl.product_id 
        FROM favorite_list fl 
        WHERE fl.user_id = :userId
    """, nativeQuery = true)
    Set<Long> findProductIdsByUserId(@Param("userId") Long userId);
}