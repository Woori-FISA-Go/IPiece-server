package com.masterpiece.IPiece.market.infra.adapter;

import com.masterpiece.IPiece.market.application.port.PrevCloseQueryPort;
import com.masterpiece.IPiece.market.infra.jpa.TradeExecutionJpaRepository;
import com.masterpiece.IPiece.market.infra.jpa.projection.PrevCloseProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PrevCloseQueryAdapter implements PrevCloseQueryPort {

    private final TradeExecutionJpaRepository repo;

    @Override
    public Map<Long, Long> findYesterdaysPrevClose(Collection<Long> productIds, ZoneId zoneId) {
        if (productIds.isEmpty()) return Collections.emptyMap();

        ZoneId effectiveZone = (zoneId != null) ? zoneId : ZoneId.of("Asia/Seoul");

        LocalDate today = LocalDate.now(effectiveZone);
        ZonedDateTime todayNine = today.atTime(9, 0, 0).atZone(effectiveZone);
        ZonedDateTime yesterdayNine = todayNine.minusDays(1);

        var rows = repo.findYesterdaysPrevClose(
                productIds,
                yesterdayNine.toOffsetDateTime(),
                todayNine.toOffsetDateTime()
        );

        return rows.stream()
                .filter(r -> r.getProduct_id() != null && r.getPrice() != null)
                .collect(Collectors.toMap(
                        PrevCloseProjection::getProduct_id,
                        PrevCloseProjection::getPrice,
                        (prev, curr) -> prev
                ));
    }
}
