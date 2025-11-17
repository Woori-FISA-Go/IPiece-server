package com.masterpiece.IPiece.main.api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainPageResponse {
    private List<Map<String, Object>> banners;
    private List<ProductCardResponse> offeringProducts;
    private List<ProductCardResponse> tradingProducts;
}
