package com.masterpiece.IPiece.mypage.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteItemDto {
    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("status")
    private String status;  // "거래" or "공모"

    @JsonProperty("thumbnail")
    private String thumbnail;

    @JsonProperty("current_price")
    private Long currentPrice;

    @JsonProperty("price_change_rate")
    private Double priceChangeRate;  // nullable

    @JsonProperty("is_favorite")
    private Boolean isFavorite;  // 항상 true
}
