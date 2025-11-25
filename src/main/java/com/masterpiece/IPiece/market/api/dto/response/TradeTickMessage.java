package com.masterpiece.IPiece.market.api.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TradeTickMessage {

    private final Long productId;
    private final Long tradePrice;
    private final Long tradeQuantity;
    private final String matchTime;
}
