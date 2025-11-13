package com.masterpiece.IPiece.market.infra.adapter;

import com.masterpiece.IPiece.market.application.port.FavoriteQueryPort;
import com.masterpiece.IPiece.market.infra.jpa.FavoriteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class FavoriteQueryAdapter implements FavoriteQueryPort {
    private final FavoriteJpaRepository repo;

    @Override
    public Set<Long> findProductIdsByUserId(Long userId) {
        return (userId == null) ? Set.of() : repo.findProductIdsByUserId(userId);
    }
}