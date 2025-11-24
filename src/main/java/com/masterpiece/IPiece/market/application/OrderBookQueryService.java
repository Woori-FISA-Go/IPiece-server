package com.masterpiece.IPiece.market.application;

import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.market.api.dto.response.OrderBookResponse;
import com.masterpiece.IPiece.market.application.port.PrevCloseQueryPort;
import com.masterpiece.IPiece.market.infra.jpa.OrderBookRepository;
import com.masterpiece.IPiece.market.infra.jpa.TradeExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderBookQueryService {

    private final ProductRepository productRepository;
    private final PrevCloseQueryPort prevCloseQueryPort;
    private final TradeExecutionRepository tradeExecutionRepository;
    private final OrderBookRepository orderBookRepository;

    public OrderBookResponse getOrderBook(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("product not found"));

        long currentPrice = product.getCurrentPrice();
        long prevClose = prevCloseQueryPort.findPrevCloseSingle(productId, ZoneId.of("Asia/Seoul")).orElse(0L);

        double priceChange = (prevClose > 0)
                ? ((double) (currentPrice - prevClose) / prevClose) * 100
                : 0.0;

        var now = OffsetDateTime.now(ZoneId.of("Asia/Seoul"));
        var oneWeekAgo = now.minusDays(7);

        Long highest = tradeExecutionRepository.findHighestPrice(productId, oneWeekAgo, now).orElse(null);
        Long lowest = tradeExecutionRepository.findLowestPrice(productId, oneWeekAgo, now).orElse(null);

        Long thisWeekVol = tradeExecutionRepository.findVolume(productId, oneWeekAgo, now);
        Long lastWeekVol = tradeExecutionRepository.findVolume(productId, now.minusDays(14), oneWeekAgo);

        long limitUp = Math.round(prevClose * 1.30);
        long limitDown = Math.round(prevClose * 0.70);

        var sells = orderBookRepository.findSellOrderLevels(productId);
        var buys = orderBookRepository.findBuyOrderLevels(productId);

        var sellItems = sells.stream()
                .map(s -> new OrderBookResponse.OrderBookItem(s.getPrice(), s.getQty(),
                        calcChangeRate(s.getPrice(), prevClose)))
                .toList();

        var buyItems = buys.stream()
                .map(b -> new OrderBookResponse.OrderBookItem(b.getPrice(), b.getQty(),
                        calcChangeRate(b.getPrice(), prevClose)))
                .toList();

        OrderBookResponse.Summary summary =
                new OrderBookResponse.Summary(
                        highest,
                        lowest,
                        currentPrice,
                        priceChange,
                        limitUp,
                        limitDown,
                        thisWeekVol != null ? thisWeekVol : 0L,
                        lastWeekVol != null ? lastWeekVol : 0L
                );

        return new OrderBookResponse(summary, sellItems, buyItems);
    }

    private double calcChangeRate(long price, long prevClose) {
        if (prevClose == 0) {
            return 0.0;
        }
        return ((double) (price - prevClose) / prevClose) * 100;
    }
}
