package com.masterpiece.IPiece.market.infra.messaging;

import com.masterpiece.IPiece.market.api.dto.response.HoldingAssetResponse;
import com.masterpiece.IPiece.market.api.dto.response.OrderBookResponse;
import com.masterpiece.IPiece.market.api.dto.response.PendingOrderListResponse;
import com.masterpiece.IPiece.market.api.dto.response.TradeTickMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishOrderBook(Long productId, OrderBookResponse response) {
        messagingTemplate.convertAndSend("/topic/orderbook/" + productId, response);
    }

    public void publishChartTick(Long productId, TradeTickMessage payload) {
        messagingTemplate.convertAndSend("/topic/chart/" + productId, payload);
    }

    public void publishHolding(Long userId, Long productId, HoldingAssetResponse response) {
        messagingTemplate.convertAndSend("/topic/holding/" + userId + "/" + productId, response);
    }

    public void publishPendingOrders(Long userId, Long productId, PendingOrderListResponse response) {
        messagingTemplate.convertAndSend("/topic/pending-orders/" + userId + "/" + productId, response);
    }

    public void publishProductPrice(Long productId, Long price) {
        messagingTemplate.convertAndSend("/topic/product/" + productId + "/price", price);
    }
}
