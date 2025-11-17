package com.masterpiece.IPiece.main.api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCardResponse {
    private Long productId;
    private String productName;
    private Long currentPrice;
    private String thumbnailImg;
}
