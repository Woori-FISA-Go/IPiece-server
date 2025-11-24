package com.masterpiece.IPiece.market.application;

import com.masterpiece.IPiece.market.api.dto.response.HoldingAssetResponse;
import com.masterpiece.IPiece.market.infra.messaging.RealtimePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingAssetPushService {

    private final HoldingAssetQueryService holdingAssetQueryService;
    private final RealtimePublisher realtimePublisher;

    public void pushAsset(Long userId, Long productId) {
        try {
            HoldingAssetResponse response = holdingAssetQueryService.getHoldingAsset(userId, productId);
            realtimePublisher.publishHolding(userId, productId, response);
        } catch (IllegalArgumentException e) {
            // 사용자가 해당 상품을 더 이상 보유하지 않는 경우 스킵
            log.debug("Skip holding push. userId={}, productId={}, reason={}",
                    userId, productId, e.getMessage());
        }
    }
}
