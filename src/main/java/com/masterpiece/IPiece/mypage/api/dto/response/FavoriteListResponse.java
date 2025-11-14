package com.masterpiece.IPiece.mypage.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.masterpiece.IPiece.mypage.api.dto.FavoriteItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteListResponse {

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("items")
    private List<FavoriteItemDto> items;
}
