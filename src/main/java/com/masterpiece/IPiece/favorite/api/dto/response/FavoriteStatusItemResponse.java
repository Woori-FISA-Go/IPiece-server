package com.masterpiece.IPiece.favorite.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FavoriteStatusItemResponse {

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("liked")
    private boolean liked;

    // liked가 false일 땐 null로 내려가도록 설정 (옵션)
    @JsonProperty("favorite_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String favoriteId;
}