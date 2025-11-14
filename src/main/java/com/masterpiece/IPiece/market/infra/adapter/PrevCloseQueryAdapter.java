package com.masterpiece.IPiece.market.infra.adapter;

import com.masterpiece.IPiece.market.application.port.PrevCloseQueryPort;
import com.masterpiece.IPiece.market.infra.jpa.TradeExecutionRepository;
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

    private final TradeExecutionRepository tradeExecutionRepository;

    @Override
    public Map<Long, Long> findPrevCloseMap(Collection<Long> productIds, ZoneId zoneId) {
        if (productIds.isEmpty()) return Collections.emptyMap();

        ZoneId effectiveZone = (zoneId != null) ? zoneId : ZoneId.of("Asia/Seoul");

        LocalDate today = LocalDate.now(effectiveZone);
        ZonedDateTime todayNine = today.atTime(9, 0, 0).atZone(effectiveZone);
        ZonedDateTime yesterdayNine = todayNine.minusDays(1);

        var rows = tradeExecutionRepository.findAllPrevClosePrices(
                productIds,
                yesterdayNine.toLocalDateTime(),
                todayNine.toLocalDateTime()
        );

        return rows.stream()
                .filter(r -> r.getProductId() != null && r.getPrice() != null)
                .collect(Collectors.toMap(
                        PrevCloseProjection::getProductId,
                        PrevCloseProjection::getPrice,
                        (prev, curr) -> prev
                ));
    }

    @Override
    public Optional<Long> findPrevCloseSingle(Long productId, ZoneId zoneId) {
        if (productId == null) return Optional.empty();

        ZoneId effectiveZone = (zoneId != null) ? zoneId : ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(effectiveZone);
        ZonedDateTime todayNine = today.atTime(9, 0, 0).atZone(effectiveZone);
        ZonedDateTime yesterdayNine = todayNine.minusDays(1);

        Long price = tradeExecutionRepository.findPrevClosePrice(
                productId,
                yesterdayNine.toLocalDateTime(),
                todayNine.toLocalDateTime()
        );

        return Optional.ofNullable(price);
    }

}
