package com.masterpiece.IPiece.mypage.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDto {

    private Long productId;
    private String productName;
    private String tokenName;
    private String presentImg;
    private Long quantity;
    private Long avgBuyPrice;
    private Long totalBuyPrice;      // avg_buy_price * quantity
    private Long currentPrice;
    private Long currentValue;       // current_price * quantity (실제로는 totalBuyPrice + profit)
    private Long profit;             // 평가손익
    private Double profitRate;       // 평가수익률
}