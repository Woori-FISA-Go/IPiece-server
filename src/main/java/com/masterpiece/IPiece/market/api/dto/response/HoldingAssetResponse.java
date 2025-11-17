package com.masterpiece.IPiece.market.api.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HoldingAssetResponse {

    private String product_name;
    private long quantity;
    private long avg_buy_price;
    private long total_amount;
    private long total_profit_amount;
    private double total_profit_rate;
}

