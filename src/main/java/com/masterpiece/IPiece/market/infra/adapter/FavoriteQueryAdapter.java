package com.masterpiece.IPiece.market.infra.adapter;

import com.masterpiece.IPiece.favorite.infra.FavoriteListRepository;
import com.masterpiece.IPiece.market.application.port.FavoriteQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class FavoriteQueryAdapter implements FavoriteQueryPort {
    private final FavoriteListRepository favoriteListRepository;

    @Override
    public Set<Long> findProductIdsByUserId(Long userId) {
        return (userId == null) ? Set.of() : favoriteListRepository.findProductIdsByUserId(userId);
    }

    @Override
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        if (userId == null) return false;
        return favoriteListRepository.existsByUser_UserIdAndProduct_ProductId(userId, productId);
    }
}
