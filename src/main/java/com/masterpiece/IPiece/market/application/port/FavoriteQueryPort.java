package com.masterpiece.IPiece.market.application.port;

import java.util.Set;

public interface FavoriteQueryPort {
    Set<Long> findProductIdsByUserId(Long userId);
}
