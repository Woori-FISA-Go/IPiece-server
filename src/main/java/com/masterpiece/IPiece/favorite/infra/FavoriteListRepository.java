package com.masterpiece.IPiece.favorite.infra;


import com.masterpiece.IPiece.favorite.domain.FavoriteList;
import com.masterpiece.IPiece.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.Optional;


public interface FavoriteListRepository extends JpaRepository<FavoriteList, Long> {

    Optional<FavoriteList> findByUserAndProduct(User user, Product product);

    // 특정 유저의 찜 목록 조회
    List<FavoriteList> findAllByUser(User user);

    @Query("""
        SELECT fl.product.productId
          FROM FavoriteList fl
         WHERE fl.user.userId = :userId
    """)
    Set<Long> findProductIdsByUserId(@Param("userId") Long userId);

    boolean existsByUser_UserIdAndProduct_ProductId(Long userId, Long productId);
}
