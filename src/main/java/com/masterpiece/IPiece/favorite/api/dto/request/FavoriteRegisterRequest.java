package com.masterpiece.IPiece.favorite.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FavoriteRegisterRequest {

    @JsonProperty("product_id")
    private String productId;   // 요청은 string으로 들어옴 (예: "501")

    /** 내부 로직에서 사용할 숫자 ID(Long)로 변환 */
    public Long toProductIdAsLong() {
        return Long.parseLong(productId);  // "501" -> 501L
    }
}