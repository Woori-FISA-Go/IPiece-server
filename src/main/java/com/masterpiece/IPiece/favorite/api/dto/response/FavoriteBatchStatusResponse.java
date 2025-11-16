package com.masterpiece.IPiece.favorite.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FavoriteBatchStatusResponse {

    @JsonProperty("results")
    private List<FavoriteStatusItemResponse> results;
}