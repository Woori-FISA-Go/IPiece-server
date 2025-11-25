package com.masterpiece.IPiece.market.application;

import com.masterpiece.IPiece.market.api.dto.response.TradeTickMessage;
import com.masterpiece.IPiece.market.infra.messaging.RealtimePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TradeTickPushService {

    private final RealtimePublisher realtimePublisher;

    public void pushTick(Long productId, Long price, Long quantity, OffsetDateTime matchTime) {
        TradeTickMessage payload = TradeTickMessage.builder()
                .productId(productId)
                .tradePrice(price)
                .tradeQuantity(quantity)
                .matchTime(matchTime.toString())
                .build();
        realtimePublisher.publishChartTick(productId, payload);
    }
}
