package com.masterpiece.IPiece.favorite.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FavoriteAlreadyLikeResponse {

    @JsonProperty("status")
    private String status;   // "ALREADY_LIKED"

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("liked")
    private boolean liked;   // true
}