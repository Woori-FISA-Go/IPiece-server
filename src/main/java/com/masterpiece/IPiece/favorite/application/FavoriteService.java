package com.masterpiece.IPiece.favorite.application;

import com.masterpiece.IPiece.favorite.domain.FavoriteList;
import com.masterpiece.IPiece.favorite.infra.FavoriteListRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteListRepository favoriteListRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 즐겨찾기 등록/중복 처리 로직
     */
    @Transactional
    public FavoriteRegisterResult registerFavorite(Long userId, Long productId) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // 3. 이미 즐겨찾기 되어 있는지 확인
        return favoriteListRepository.findByUserAndProduct(user, product)
                .map(FavoriteRegisterResult::alreadyLiked)
                .orElseGet(() -> {
                    // 4. 없으면 새로 저장
                    FavoriteList saved = favoriteListRepository.save(
                            FavoriteList.builder()
                                    .user(user)
                                    .product(product)
                                    .build()
                    );
                    return FavoriteRegisterResult.created(saved);
                });
    }

    /* ===== 서비스 결과 ===== */

    @Getter
    public static class FavoriteRegisterResult {
        private final FavoriteList favorite;
        private final boolean alreadyLiked;

        private FavoriteRegisterResult(FavoriteList favorite, boolean alreadyLiked) {
            this.favorite = favorite;
            this.alreadyLiked = alreadyLiked;
        }

        public static FavoriteRegisterResult alreadyLiked(FavoriteList favorite) {
            return new FavoriteRegisterResult(favorite, true);
        }

        public static FavoriteRegisterResult created(FavoriteList favorite) {
            return new FavoriteRegisterResult(favorite, false);
        }

        public OffsetDateTime getCreatedAt() {
            // BaseEntity에 createdAt(LocalDateTime) 있다고 가정
            return favorite.getCreateAt().atOffset(ZoneOffset.UTC);
        }
    }

    /* ===== 예외 ===== */

    @Getter
    public static class ProductNotFoundException extends RuntimeException {
        private final Long productId;

        public ProductNotFoundException(Long productId) {
            super("상품을 찾을 수 없습니다. id=" + productId);
            this.productId = productId;
        }

    }

    @Getter
    public static class UserNotFoundException extends RuntimeException {
        private final Long userId;

        public UserNotFoundException(Long userId) {
            super("유저를 찾을 수 없습니다. id=" + userId);
            this.userId = userId;
        }

    }
}