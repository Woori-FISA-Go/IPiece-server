package com.masterpiece.IPiece.favorite.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FavoriteRegisterResponse {

    @JsonProperty("favorite_id")
    private String favoriteId;

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("liked")
    private boolean liked;

    @JsonProperty("created_at")
    private String createdAt;
}