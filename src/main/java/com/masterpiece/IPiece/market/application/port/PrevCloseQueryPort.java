package com.masterpiece.IPiece.market.application.port;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface PrevCloseQueryPort {
    Map<Long, Long> findPrevCloseMap(Collection<Long> productIds, ZoneId zoneId);

    Optional<Long> findPrevCloseSingle(Long productId, ZoneId zoneId);
}
