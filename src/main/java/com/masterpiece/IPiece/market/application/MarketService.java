package com.masterpiece.IPiece.market.application;

import com.masterpiece.IPiece.common.domain.product.Disclosure;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.product.ProductTradingInfo;
import com.masterpiece.IPiece.common.domain.product.policy.PriceChangePolicy;
import com.masterpiece.IPiece.dividends.infra.DividendPayoutsRepository;
import com.masterpiece.IPiece.dividends.infra.DividendsRepository;
import com.masterpiece.IPiece.market.api.dto.response.ProductDetailsResponse;
import com.masterpiece.IPiece.market.api.dto.response.ProductListResponse;
import com.masterpiece.IPiece.market.application.mapper.ProductMapper;
import com.masterpiece.IPiece.market.application.port.FavoriteQueryPort;
import com.masterpiece.IPiece.market.application.port.PrevCloseQueryPort;
import com.masterpiece.IPiece.market.application.port.ProductQueryPort;
import com.masterpiece.IPiece.market.application.port.TradingInfoQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarketService {

    private final ProductQueryPort productQueryPort;
    private final FavoriteQueryPort favoriteQueryPort;
    private final PrevCloseQueryPort prevCloseQueryPort;
    private final TradingInfoQueryPort tradingInfoPort;
    private final DividendsRepository dividendsRepo;
    private final DividendPayoutsRepository dividendPayoutsRepository;
    private final ProductMapper productMapper;

    public ProductListResponse getProducts(Pageable pageable, Long userId) {
        Page<com.masterpiece.IPiece.common.domain.product.Product> page =
                productQueryPort.findActiveProducts(pageable);

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

        return new ProductListResponse(items, (int) page.getTotalElements(), page.getNumber()+1);
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

        boolean isFavorited = favoriteQueryPort.existsByUserIdAndProductId(userId, productId);

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
}
