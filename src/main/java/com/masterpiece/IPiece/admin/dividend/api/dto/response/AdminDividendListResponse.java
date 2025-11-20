package com.masterpiece.IPiece.admin.dividend.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDividendListResponse {

    @JsonProperty("items")
    private List<AdminDividendResponse> items;

    @JsonProperty("total_count")
    private Long totalCount;
}