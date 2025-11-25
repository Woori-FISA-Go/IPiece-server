package com.masterpiece.IPiece.market.application;

import com.masterpiece.IPiece.market.api.dto.response.OrderBookResponse;
import com.masterpiece.IPiece.market.infra.messaging.RealtimePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderBookPushService {

    private final OrderBookQueryService orderBookQueryService;
    private final RealtimePublisher realtimePublisher;

    public void pushOrderBook(Long productId) {
        OrderBookResponse response = orderBookQueryService.getOrderBook(productId);
        realtimePublisher.publishOrderBook(productId, response);
    }
}
