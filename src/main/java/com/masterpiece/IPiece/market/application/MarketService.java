package com.masterpiece.IPiece.market.application;

import java.time.Instant;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.domain.product.Disclosure;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.product.ProductTradingInfo;
import com.masterpiece.IPiece.common.domain.product.policy.PriceChangePolicy;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.dividends.infra.DividendPayoutsRepository;
import com.masterpiece.IPiece.market.api.dto.request.OrderRequest;
import com.masterpiece.IPiece.market.api.dto.response.*;
import com.masterpiece.IPiece.market.application.mapper.ProductMapper;
import com.masterpiece.IPiece.market.application.OrderBookPushService;
import com.masterpiece.IPiece.market.application.port.FavoriteQueryPort;
import com.masterpiece.IPiece.market.application.port.PrevCloseQueryPort;
import com.masterpiece.IPiece.market.application.port.ProductQueryPort;
import com.masterpiece.IPiece.market.application.port.TradingInfoQueryPort;
import com.masterpiece.IPiece.market.domain.OrderBook;
import com.masterpiece.IPiece.market.domain.OrderType;
import com.masterpiece.IPiece.market.domain.TradeExecution;
import com.masterpiece.IPiece.market.infra.jpa.OrderBookRepository;
import com.masterpiece.IPiece.market.infra.jpa.TradeExecutionRepository;
import com.masterpiece.IPiece.mypage.domain.Holdings;
import com.masterpiece.IPiece.mypage.infra.HoldingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarketService {

    private final ProductQueryPort productQueryPort;
    private final FavoriteQueryPort favoriteQueryPort;
    private final PrevCloseQueryPort prevCloseQueryPort;
    private final TradingInfoQueryPort tradingInfoPort;

    private final DividendPayoutsRepository dividendPayoutsRepository;
    private final ProductRepository productRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final TradeExecutionRepository tradeExecutionRepository;
    private final OrderBookRepository orderBookRepository;
    private final HoldingsRepository holdingsRepository;

    private final ProductMapper productMapper;

    private final OrderMatchingService orderMatchingService;
    private final OrderBookQueryService orderBookQueryService;
    private final OrderBookPushService orderBookPushService;
    private final PendingOrderPushService pendingOrderPushService;
    private final HoldingAssetQueryService holdingAssetQueryService;


    public ProductListResponse getProducts(Pageable pageable, Long userId) {
        Page<com.masterpiece.IPiece.common.domain.product.Product> page =
                productQueryPort.findTradeProducts(pageable);

        Set<Long> favorites = (userId != null)
                ? favoriteQueryPort.findProductIdsByUserId(userId)
                : Set.of();

        List<Long> productIds = page.getContent().stream()
                .map(p -> p.getProductId())
                .collect(Collectors.toList());

        Map<Long, Long> prevCloseMap =
                prevCloseQueryPort.findPrevCloseMap(productIds, ZoneId.of("Asia/Seoul"));

        var items = page.getContent().stream()
                .map(p -> productMapper.toProductListItem(p, prevCloseMap, favorites))
                .toList();

        return new ProductListResponse(items, (int) page.getTotalElements(), page.getNumber() + 1);
    }

    public ProductDetailsResponse getDetails(Long productId, Long userId) {

        Product product = productQueryPort.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("product not found"));

        ProductTradingInfo tradingInfo = tradingInfoPort.findTradingInfo(productId).orElse(null);
        List<Disclosure> disclosures = tradingInfoPort.findDisclosures(productId, 10);

        long currentPrice = product.getCurrentPrice();
        long prevClose = prevCloseQueryPort
                .findPrevCloseSingle(productId, ZoneId.of("Asia/Seoul"))
                .orElse(0L);
        double changeRate = PriceChangePolicy.changeRate(currentPrice, prevClose);

        boolean isFavorited = (userId != null)
                && favoriteQueryPort.existsByUserIdAndProductId(userId, productId);

        var payouts = dividendPayoutsRepository
                .findByDividends_Product_ProductIdAndPayoutStatusOrderByPayoutDateDesc(
                        productId,
                        "PAID"
                );

        long tokenQuantity = product.getTokenQuantity() != null ? product.getTokenQuantity() : 0L;
        var df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<ProductDetailsResponse.DividendItem> dividendItems = payouts.stream().map(dp -> {
            long totalAmount = dp.getDividends().getTotalAmount();
            long amountPerToken = (tokenQuantity > 0)
                    ? totalAmount / tokenQuantity
                    : 0L;

            String payoutDateStr = dp.getPayoutDate().toLocalDate().format(df);

            return ProductDetailsResponse.DividendItem.builder()
                    .dividend_id(dp.getDividends().getDividendId())
                    .amount_per_token(amountPerToken)
                    .payment_date(payoutDateStr)
                    .build();
        }).toList();

        DateTimeFormatter noticeDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<ProductDetailsResponse.NoticeItem> noticeItems = disclosures.stream()
                .map(d -> ProductDetailsResponse.NoticeItem.builder()
                        .disclosure_date(d.getDisclosureDate().toLocalDate().format(noticeDateFormatter))
                        .disclosure_title(d.getDisclosureTitle())
                        .disclosure_url(d.getDisclosureUrl())
                        .build())
                .toList();

        ProductDetailsResponse.Info info = ProductDetailsResponse.Info.builder()
                .product_id(product.getProductId())
                .product_name(product.getProductName())
                .token_unit(product.getTokenName())
                .current_price(currentPrice)
                .change_rate(changeRate)
                .thumbnail_img(product.getThumbnailImg())
                .is_favorited(isFavorited)
                .build();

        ProductDetailsResponse.Details details = ProductDetailsResponse.Details.builder()
                .owner(product.getOwner())
                .total_supply(product.getTokenQuantity())
                .token_standard(product.getTokenStandard())
                .exchange_listing(product.getExchangeListing())
                .description(tradingInfo != null ? tradingInfo.getIpDescription() : null)
                .detail_image(product.getPresentImg())
                .detail_url(tradingInfo != null ? tradingInfo.getProductPageImg() : null)
                .dividends(dividendItems)
                .notices(noticeItems)
                .build();

        return ProductDetailsResponse.builder()
                .info(info)
                .details(details)
                .build();
    }

    public ChartResponse getChart(Long productId,
                                  String interval,
                                  String cursorOptional) {

        validateInterval(interval);

        ZoneId timezone = ZoneId.of("Asia/Seoul");
        OffsetDateTime now = OffsetDateTime.now(timezone);

        long windowDays = switch (interval) {
            case "1d" -> 30L;
            case "1w" -> 60L;
            case "1m" -> 90L;
            default -> throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        };

        ZonedDateTime lastDayStartZoned;
        if (cursorOptional == null) {
            lastDayStartZoned = now.atZoneSameInstant(timezone)
                    .truncatedTo(ChronoUnit.DAYS);
        } else {
            String[] parts = cursorOptional.split(":");
            if (parts.length < 2) {
                throw new IllegalArgumentException("Invalid cursor format");
            }
            try {
                long epoch = Long.parseLong(parts[1]);
                lastDayStartZoned = Instant.ofEpochSecond(epoch)
                        .atZone(timezone)
                        .truncatedTo(ChronoUnit.DAYS);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid cursor epoch value", e);
            }
        }

        ZonedDateTime windowStartZoned = lastDayStartZoned.minusDays(windowDays - 1);
        ZonedDateTime windowEndZoned = lastDayStartZoned.plusDays(1).minusSeconds(1);

        OffsetDateTime start = windowStartZoned.toOffsetDateTime();
        OffsetDateTime end = windowEndZoned.toOffsetDateTime();

        List<TradeExecution> executions =
                tradeExecutionRepository.findInWindow(productId, start, end);

        List<ChartResponse.Point> points = aggregateByInterval(executions, interval, timezone).stream()
                .map(p -> {
                    OffsetDateTime displayTs = "1d".equals(interval)
                            ? (p.lastMatch() != null ? p.lastMatch() : p.ts())
                            : p.ts();
                    return ChartResponse.Point.builder()
                            .ts(formatTimestamp(displayTs, interval))
                        .price(p.price())
                        .volume(p.volume())
                            .build();
                })
                .toList();

        ZonedDateTime prevLastDayStartZoned = lastDayStartZoned.minusDays(windowDays);
        long epochCursor = prevLastDayStartZoned.toEpochSecond();
        String nextCursor = "c:" + epochCursor + ":" + interval;

        ZonedDateTime prevWindowStartZoned = prevLastDayStartZoned.minusDays(windowDays - 1);
        ZonedDateTime prevWindowEndZoned = prevLastDayStartZoned.plusDays(1).minusSeconds(1);

        OffsetDateTime prevStart = prevWindowStartZoned.toOffsetDateTime();
        OffsetDateTime prevEnd = prevWindowEndZoned.toOffsetDateTime();

        boolean hasMore = !tradeExecutionRepository
                .findInWindow(productId, prevStart, prevEnd)
                .isEmpty();

        return ChartResponse.builder()
                .product_id(productId)
                .interval(interval)
                .window(ChartResponse.Window.builder()
                        .start_at(start.toString())
                        .end_at(end.toString())
                        .build())
                .points(points)
                .has_more(hasMore)
                .next_cursor(nextCursor)
                .fetched_at(now.toString())
                .build();
    }

    private void validateInterval(String interval) {
        if (interval == null || interval.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        Set<String> allowed = Set.of("1d", "1w", "1m");
        if (!allowed.contains(interval)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private record AggregatedPoint(OffsetDateTime ts, OffsetDateTime lastMatch, Long price, Long volume) {}

    private List<AggregatedPoint> aggregateByInterval(List<TradeExecution> executions, String interval, ZoneId zoneId) {
        Map<OffsetDateTime, AggregatedPoint> buckets = new LinkedHashMap<>();

        executions.stream()
                .sorted((a, b) -> a.getMatchTime().compareTo(b.getMatchTime()))
                .forEach(exec -> {
                    OffsetDateTime match = exec.getMatchTime();
                    ZonedDateTime zoned = match.atZoneSameInstant(zoneId);
                    OffsetDateTime bucketStart;
                    switch (interval) {
                        case "1d" -> bucketStart = zoned.truncatedTo(ChronoUnit.DAYS).toOffsetDateTime();
                        case "1w" -> bucketStart = zoned.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                                .truncatedTo(ChronoUnit.DAYS)
                                .toOffsetDateTime();
                        case "1m" -> bucketStart = zoned
                                .with(TemporalAdjusters.firstDayOfMonth())
                                .truncatedTo(ChronoUnit.DAYS)
                                .toOffsetDateTime();
                        default -> throw new BusinessException(ErrorCode.VALIDATION_ERROR);
                    }

                    AggregatedPoint current = buckets.get(bucketStart);
                    if (current == null) {
                        buckets.put(bucketStart, new AggregatedPoint(bucketStart, match, exec.getTradePrice(), exec.getTradeQuantity()));
                    } else {
                        long newVolume = current.volume() + exec.getTradeQuantity();
                        long newPrice = exec.getTradePrice(); // 마지막 체결가
                        buckets.put(bucketStart, new AggregatedPoint(bucketStart, match, newPrice, newVolume));
                    }
                });

        return List.copyOf(buckets.values());
    }

    private String formatTimestamp(OffsetDateTime ts, String interval) {
        if ("1d".equals(interval)) {
            return ts.toString();
        }
        return ts.toString();
    }

    @Transactional(readOnly = false)
    public OrderResponse buy(Long productId, Long userId, String idempotencyKeyHeader, OrderRequest req) {

        VirtualAccount account = virtualAccountRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Virtual account not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        String idempotencyKey = (idempotencyKeyHeader == null || idempotencyKeyHeader.isBlank())
                ? UUID.randomUUID().toString()
                : idempotencyKeyHeader;

        orderBookRepository.findByIdempotencyKey(idempotencyKey)
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_ORDER);
                });

        long price = req.getOrder_price();
        long quantity = req.getOrder_quantity();
        long totalAmount = price * quantity;

        if (account.getBalanceKrw() < totalAmount) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }

        account.decreaseBalanceKrw(totalAmount);
        account.increasePendingPrice(totalAmount);

        OffsetDateTime clientTimeOffset = OffsetDateTime.parse(req.getClient_time());

        OffsetDateTime now = OffsetDateTime.now();
        OrderBook order = OrderBook.builder()
                .product(product)
                .virtualAccount(account)
                .orderType(OrderType.BUY)
                .orderPrice(price)
                .orderQuantity(quantity)
                .remainQuantity(quantity)
                .pendingStatus(true)
                .clientTime(clientTimeOffset)
                .idempotencyKey(idempotencyKey)
                .build();

        OrderBook savedOrder;
        try {
            savedOrder = orderBookRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            // 동시성으로 unique constraint가 먼저 걸린 경우
            throw new BusinessException(ErrorCode.DUPLICATE_ORDER);
        }
        orderBookPushService.pushOrderBook(productId);
        pendingOrderPushService.pushPendingOrders(userId, productId);
        registerAfterCommitMatching(savedOrder.getOrderId());

        return OrderResponse.builder()
                .status_code(200)
                .order_id(savedOrder.getOrderId().toString())
                .product_id(productId)
                .side("BUY")
                .order_price(price)
                .order_quantity(quantity)
                .total_amount(totalAmount)
                .filled_quantity(0L)
                .remaining_quantity(quantity)
                .created_at(now.toString())
                .idempotency_key(idempotencyKey)
                .build();
    }

    @Transactional(readOnly = false)
    public OrderResponse sell(Long productId, Long userId, String idempotencyKeyHeader, OrderRequest req) {

        VirtualAccount account = virtualAccountRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Virtual account not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        String idempotencyKey = (idempotencyKeyHeader == null || idempotencyKeyHeader.isBlank())
                ? UUID.randomUUID().toString()
                : idempotencyKeyHeader;

        orderBookRepository.findByIdempotencyKey(idempotencyKey)
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_ORDER);
                });

        long price = req.getOrder_price();
        long quantity = req.getOrder_quantity();
        long totalAmount = price * quantity;

        Holdings holdings = holdingsRepository.findByVirtualAccountAndProduct(account, product)
                .orElseThrow(() -> new IllegalStateException("보유 수량이 부족합니다."));

        if (holdings.getQuantity() < quantity) {
            throw new IllegalStateException("보유 수량이 부족합니다.");
        }

        holdings.moveToPending(quantity);

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime clientTimeOffset = OffsetDateTime.parse(req.getClient_time());

        OrderBook order = OrderBook.builder()
                .product(product)
                .virtualAccount(account)
                .orderType(OrderType.SELL)
                .orderPrice(price)
                .orderQuantity(quantity)
                .remainQuantity(quantity)
                .pendingStatus(true)
                .clientTime(clientTimeOffset)
                .idempotencyKey(idempotencyKey)
                .build();

        OrderBook savedOrder;
        try {
            savedOrder = orderBookRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_ORDER);
        }
        orderBookPushService.pushOrderBook(productId);
        pendingOrderPushService.pushPendingOrders(userId, productId);
        registerAfterCommitMatching(savedOrder.getOrderId());

        return OrderResponse.builder()
                .status_code(200)
                .order_id(savedOrder.getOrderId().toString())
                .product_id(productId)
                .side("SELL")
                .order_price(price)
                .order_quantity(quantity)
                .total_amount(totalAmount)
                .filled_quantity(0L)
                .remaining_quantity(quantity)
                .created_at(now.toString())
                .idempotency_key(idempotencyKey)
                .build();
    }

    public PendingOrderListResponse getPendingOrders(Long userId, Long productId, int page) {

        if (page < 1) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        int pageIndex = page - 1; // JPA는 0-base 페이지
        int size = 10;

        var pageable = PageRequest.of(pageIndex, size);
        var result = orderBookRepository.findPendingOrders(userId, productId, pageable);

        var items = result.getContent().stream().map(ob -> {
            long remain = ob.getRemainQuantity() == null ? 0L : ob.getRemainQuantity();
            long filled = ob.getOrderQuantity() - remain;
            return PendingOrderItem.builder()
                    .order_id(String.valueOf(ob.getOrderId()))
                    .product_id(productId)
                    .product_name(ob.getProduct().getProductName())
                    .order_type(ob.getOrderType().name())
                    .price(ob.getOrderPrice())
                    .quantity(ob.getOrderQuantity())
                    .filled_quantity(filled)
                    .remaining_quantity(remain)
                    .amount(ob.getOrderPrice() * ob.getOrderQuantity())
                    .placed_at(ob.getClientTime().toString())
                    .build();
        }).collect(Collectors.toList());

        return PendingOrderListResponse.builder()
                .items(items)
                .page(page)
                .total(result.getTotalElements())
                .has_next(result.hasNext())
                .build();
    }

    @Transactional(readOnly = true)
    public HoldingAssetResponse getHoldingAsset(Long userId, Long productId) {
        return holdingAssetQueryService.getHoldingAsset(userId, productId);
    }

    public OrderBookResponse getOrderBook(Long productId) {
        return orderBookQueryService.getOrderBook(productId);
    }

    private void registerAfterCommitMatching(Long orderId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void suspend() {}

                @Override
                public void resume() {}

                @Override
                public void flush() {}

                @Override
                public void beforeCommit(boolean readOnly) {}

                @Override
                public void beforeCompletion() {}

                @Override
                public void afterCommit() {
                    orderMatchingService.matchWithRetry(orderId);
                }

                @Override
                public void afterCompletion(int status) {}
            });
        } else {
            orderMatchingService.matchWithRetry(orderId);
        }
    }
}
