package com.masterpiece.IPiece.market.application.port;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Map;

public interface PrevCloseQueryPort {
    Map<Long, Long> findYesterdaysPrevClose(Collection<Long> productIds, ZoneId zoneId);
}
