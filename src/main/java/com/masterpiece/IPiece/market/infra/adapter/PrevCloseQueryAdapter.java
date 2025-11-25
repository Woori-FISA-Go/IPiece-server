package com.masterpiece.IPiece.market.infra.adapter;

import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.market.application.port.PrevCloseQueryPort;
import com.masterpiece.IPiece.market.infra.jpa.TradeExecutionRepository;
import com.masterpiece.IPiece.market.infra.jpa.projection.PrevCloseProjection;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrevCloseQueryAdapter implements PrevCloseQueryPort {

    private final TradeExecutionRepository tradeExecutionRepository;
    private final ProductRepository productRepository;

    @Override
    public Map<Long, Long> findPrevCloseMap(Collection<Long> productIds, ZoneId zoneId) {
        if (productIds.isEmpty()) return Collections.emptyMap();

        ZoneId effectiveZone = (zoneId != null) ? zoneId : ZoneId.of("Asia/Seoul");

        LocalDate today = LocalDate.now(effectiveZone);
        ZonedDateTime todayNine = today.atTime(9, 0, 0).atZone(effectiveZone);
        ZonedDateTime yesterdayNine = todayNine.minusDays(1);
        ZonedDateTime windowStart = yesterdayNine;

        var rows = tradeExecutionRepository.findAllPrevClosePrices(
                productIds,
                windowStart.toOffsetDateTime(),
                todayNine.toOffsetDateTime()
        );

        Map<Long, Long> map = rows.stream()
                .filter(r -> r.getProductId() != null && r.getPrice() != null)
                .collect(Collectors.toMap(
                        PrevCloseProjection::getProductId,
                        PrevCloseProjection::getPrice,
                        (prev, curr) -> prev
                ));

        // fallback: fill missing products with lastPrice (or currentPrice) to avoid zero default
        productIds.forEach(pid -> {
            if (!map.containsKey(pid)) {
                Long fallback = fallbackPrevClose(pid, windowStart.toOffsetDateTime());
                if (fallback != null) {
                    map.put(pid, fallback);
                }
            }
        });

        return map;
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
                yesterdayNine.toOffsetDateTime(),
                todayNine.toOffsetDateTime()
        );

        if (price == null) {
            price = fallbackPrevClose(productId, yesterdayNine.toOffsetDateTime());
        }

        return Optional.ofNullable(price);
    }

    private Long fallbackPrevClose(Long productId, OffsetDateTime before) {
        Long latestBeforeWindow = tradeExecutionRepository.findLatestPriceBefore(productId, before);
        if (latestBeforeWindow != null) {
            return latestBeforeWindow;
        }
        return productRepository.findById(productId)
                .map(p -> p.getLastPrice() != null ? p.getLastPrice() : p.getCurrentPrice())
                .orElse(null);
    }
}
