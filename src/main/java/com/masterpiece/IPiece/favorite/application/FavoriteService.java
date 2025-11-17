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
import java.util.List;
import java.util.Map;

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

    /**
     * 즐겨찾기 해제
     * - (userId, productId)에 해당하는 FavoriteList를 찾아 삭제
     * - 없으면 FavoriteNotFoundException
     */
    @Transactional
    public FavoriteUnlikeResult unregisterFavorite(Long userId, Long productId) {
        FavoriteList favorite = favoriteListRepository
                .findByUser_UserIdAndProduct_ProductId(userId, productId)
                .orElseThrow(() -> new FavoriteNotFoundException(userId, productId));

        Long favoriteId = favorite.getFavoriteId();

        // 응답용 시간은 "지금" 기준으로 사용 (삭제 직전 기준)
        OffsetDateTime updatedAt = OffsetDateTime.now(ZoneOffset.UTC);

        favoriteListRepository.delete(favorite);

        return new FavoriteUnlikeResult(favoriteId, updatedAt);
    }

    /**
     * 렌더용 배치 조회
     * - 하나의 유저에 대해 여러 productId에 대한 즐겨찾기 여부를 한 번에 조회
     */
    @Transactional
    public List<FavoriteStatusItem> getFavoriteStatusBatch(Long userId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        // 1. 해당 유저 + productIds 전체에 대한 즐겨찾기 목록을 한 번에 조회
        List<FavoriteList> favorites = favoriteListRepository
                .findByUser_UserIdAndProduct_ProductIdIn(userId, productIds);

        // 2. productId -> FavoriteList 맵 구성
        Map<Long, FavoriteList> favoriteMap = favorites.stream()
                .collect(java.util.stream.Collectors.toMap(
                        fl -> fl.getProduct().getProductId(),
                        fl -> fl
                ));

        // 3. 요청된 productIds 순서를 유지하며 결과 리스트 생성
        return productIds.stream()
                .distinct() // 동일 productId 중복 요청 방지
                .map(pid -> {
                    FavoriteList fl = favoriteMap.get(pid);
                    if (fl != null) {
                        return new FavoriteStatusItem(
                                pid,
                                true,
                                fl.getFavoriteId()
                        );
                    } else {
                        return new FavoriteStatusItem(
                                pid,
                                false,
                                null
                        );
                    }
                })
                .toList();
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
            return favorite.getCreatedAt().atOffset(ZoneOffset.UTC);
        }
    }

    @Getter
    public static class FavoriteUnlikeResult {
        private final Long favoriteId;
        private final OffsetDateTime updatedAt;

        public FavoriteUnlikeResult(Long favoriteId, OffsetDateTime updatedAt) {
            this.favoriteId = favoriteId;
            this.updatedAt = updatedAt;
        }
    }

    @Getter
    public static class FavoriteStatusItem {
        private final Long productId;
        private final boolean liked;
        private final Long favoriteId; // null if not liked

        public FavoriteStatusItem(Long productId, boolean liked, Long favoriteId) {
            this.productId = productId;
            this.liked = liked;
            this.favoriteId = favoriteId;
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

    @Getter
    public static class FavoriteNotFoundException extends RuntimeException {
        private final Long userId;
        private final Long productId;

        public FavoriteNotFoundException(Long userId, Long productId) {
            super("favorite_id가 존재하지 않습니다. userId=%d, productId=%d".formatted(userId, productId));
            this.userId = userId;
            this.productId = productId;
        }
    }
}