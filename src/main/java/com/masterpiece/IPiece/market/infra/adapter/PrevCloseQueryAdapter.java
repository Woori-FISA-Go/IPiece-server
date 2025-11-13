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
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PrevCloseQueryAdapter implements PrevCloseQueryPort {

    private final TradeExecutionJpaRepository repo;

    @Override
    public Map<Long, Long> findPrevCloseMap(Collection<Long> productIds, ZoneId zoneId) {
        if (productIds.isEmpty()) return Collections.emptyMap();

        ZoneId effectiveZone = (zoneId != null) ? zoneId : ZoneId.of("Asia/Seoul");

        LocalDate today = LocalDate.now(effectiveZone);
        ZonedDateTime todayNine = today.atTime(9, 0, 0).atZone(effectiveZone);
        ZonedDateTime yesterdayNine = todayNine.minusDays(1);

        var rows = repo.findAllPrevClosePrices(
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

    @Override
    public Optional<Long> findPrevCloseSingle(Long productId, ZoneId zoneId) {
        if (productId == null) return Optional.empty();

        ZoneId effectiveZone = (zoneId != null) ? zoneId : ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(effectiveZone);
        ZonedDateTime todayStart = today.atStartOfDay(effectiveZone);
        ZonedDateTime ydayStart  = todayStart.minusDays(1);

        Long price = repo.findPrevClosePrice(
                productId,
                ydayStart.toOffsetDateTime(),
                todayStart.toOffsetDateTime()
        );

        return Optional.ofNullable(price);
    }

}
