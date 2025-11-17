package com.masterpiece.IPiece.market.application;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.common.domain.infra.VirtualAccountRepository;
import com.masterpiece.IPiece.common.domain.product.Disclosure;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.product.ProductTradingInfo;
import com.masterpiece.IPiece.common.domain.product.policy.PriceChangePolicy;
import com.masterpiece.IPiece.dividends.infra.DividendPayoutsRepository;
import com.masterpiece.IPiece.market.api.dto.request.OrderRequest;
import com.masterpiece.IPiece.market.api.dto.response.*;
import com.masterpiece.IPiece.market.application.mapper.ProductMapper;
import com.masterpiece.IPiece.market.application.port.FavoriteQueryPort;
import com.masterpiece.IPiece.market.application.port.PrevCloseQueryPort;
import com.masterpiece.IPiece.market.application.port.ProductQueryPort;
import com.masterpiece.IPiece.market.application.port.TradingInfoQueryPort;
import com.masterpiece.IPiece.market.domain.OrderBook;
import com.masterpiece.IPiece.market.domain.OrderType;
import com.masterpiece.IPiece.market.infra.jpa.OrderBookRepository;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final OrderBookRepository orderBookRepository;

    private final ProductMapper productMapper;

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
                    throw new DuplicateRequestException(
                            "Duplicate idempotency key: " + idempotencyKey
                    );
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
        LocalDateTime clientTime = clientTimeOffset.toLocalDateTime();

        OffsetDateTime now = OffsetDateTime.now();
        OrderBook order = OrderBook.builder()
                .product(product)
                .virtualAccount(account)
                .orderType(OrderType.BUY)
                .orderPrice(price)
                .orderQuantity(quantity)
                .remainQuantity(quantity)
                .pendingStatus(true)
                .clientTime(clientTimeOffset.toLocalDateTime())
                .idempotencyKey(idempotencyKey)
                .build();

        OrderBook saved = orderBookRepository.save(order);

        return OrderResponse.builder()
                .status_code(200)
                .order_id(saved.getOrderId().toString())
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
                    throw new DuplicateRequestException(
                            "Duplicate idempotency key: " + idempotencyKey
                    );
                });

        long price = req.getOrder_price();
        long quantity = req.getOrder_quantity();
        long totalAmount = price * quantity;

        // ================================
        // TODO: 보유자산 테이블에서 해당 IP 토큰 보유 수량 조회
        //  - 만약 보유 수량 < quantity 이면 예외 던지기
        //
        // long holdingQty = holdingRepository
        //      .findQuantityByAccountAndProduct(account, product);
        // if (holdingQty < quantity) {
        //     throw new IllegalStateException("보유 수량이 부족합니다.");
        // }
        //
        // TODO: 보유자산 테이블에서 가용 수량 줄이고, "판매 대기" 수량으로 이동
        // ================================

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
                .clientTime(clientTimeOffset.toLocalDateTime())
                .idempotencyKey(idempotencyKey)
                .build();

        OrderBook saved = orderBookRepository.save(order);

        return OrderResponse.builder()
                .status_code(200)
                .order_id(saved.getOrderId().toString())
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

        int pageIndex = page - 1; // JPA는 0-base 페이지
        int size = 10;

        var pageable = PageRequest.of(pageIndex, size);
        var result = orderBookRepository.findPendingOrders(userId, productId, pageable);

        var items = result.getContent().stream().map(ob ->
                PendingOrderItem.builder()
                        .order_id(String.valueOf(ob.getOrderId()))
                        .product_id(productId)
                        .product_name(ob.getProduct().getProductName())
                        .order_type(ob.getOrderType().name())
                        .price(ob.getOrderPrice())
                        .quantity(ob.getOrderQuantity())
                        .filled_quantity(ob.getOrderQuantity() - ob.getRemainQuantity())
                        .remaining_quantity(ob.getRemainQuantity())
                        .amount(ob.getOrderPrice() * ob.getOrderQuantity())
                        .placed_at(ob.getClientTime().toString())
                        .build()
        ).collect(Collectors.toList());

        return PendingOrderListResponse.builder()
                .items(items)
                .page(page)
                .total(result.getTotalElements())
                .has_next(result.hasNext())
                .build();
    }
}
