package com.masterpiece.IPiece.market.infra.adapter;

import com.masterpiece.IPiece.market.application.port.FavoriteQueryPort;
import com.masterpiece.IPiece.market.infra.jpa.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class FavoriteQueryAdapter implements FavoriteQueryPort {
    private final FavoriteRepository favoriteRepository;

    @Override
    public Set<Long> findProductIdsByUserId(Long userId) {
        return (userId == null) ? Set.of() : favoriteRepository.findProductIdsByUserId(userId);
    }

    @Override
    public boolean existsByUserIdAndProductId(Long userId, Long productId) {
        if (userId == null) return false;
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }
}