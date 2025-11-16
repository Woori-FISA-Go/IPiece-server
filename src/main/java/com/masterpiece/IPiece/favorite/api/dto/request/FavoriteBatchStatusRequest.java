package com.masterpiece.IPiece.favorite.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class FavoriteBatchStatusRequest {

    @JsonProperty("product_ids")
    private List<String> productIds;   // 예: ["1","2","3"]

    public boolean isEmpty() {
        return productIds == null || productIds.isEmpty();
    }

    public int size() {
        return productIds == null ? 0 : productIds.size();
    }

    /** 내부 로직에서 사용할 숫자 ID(Long) 리스트로 변환 */
    public List<Long> toProductIdLongList() {
        return productIds.stream()
                .map(Long::parseLong)   // "1" -> 1L
                .toList();
    }
}