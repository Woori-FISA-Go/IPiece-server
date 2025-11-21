package com.masterpiece.IPiece.market.infra.messaging;

import com.masterpiece.IPiece.market.api.dto.response.OrderBookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderBookPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishOrderBook(Long productId, OrderBookResponse response) {
        messagingTemplate.convertAndSend(
                "/topic/orderbook/" + productId,
                response
        );
    }
}