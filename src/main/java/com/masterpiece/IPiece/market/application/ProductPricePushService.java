package com.masterpiece.IPiece.market.application;

import com.masterpiece.IPiece.market.infra.messaging.RealtimePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductPricePushService {

    private final RealtimePublisher realtimePublisher;

    public void pushPrice(Long productId, Long price) {
        realtimePublisher.publishProductPrice(productId, price);
    }
}
