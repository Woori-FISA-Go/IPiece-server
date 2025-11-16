package com.masterpiece.IPiece.favorite.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FavoriteUnlikeResponse {

    @JsonProperty("status")
    private String status;        // "UNLIKED"

    @JsonProperty("favorite_id")
    private String favoriteId;

    @JsonProperty("liked")
    private boolean liked;       // 항상 false

    @JsonProperty("updated_at")
    private String updatedAt;    // ISO-8601 문자열
}