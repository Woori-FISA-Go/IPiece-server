package com.masterpiece.IPiece.mypage.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * /v1/mypage/account 의 history 배열 한 건을 표현하는 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountHistoryItemDto {

    @JsonProperty("created_at")
    private String createdAt;      // "yyyy-MM-dd HH:mm"

    @JsonProperty("type")
    private String type;           // "구매", "판매", "배당금" 등

    @JsonProperty("description")
    private String description;    // 예: "다이노탱 토큰 구매"

    @JsonProperty("token_amount")
    private Long tokenAmount;      // 구매: +수량, 판매: -수량, 배당금/입출금: null

    @JsonProperty("price")
    private Long price;            // 구매: -현금, 판매: +현금, 배당금: +현금
}