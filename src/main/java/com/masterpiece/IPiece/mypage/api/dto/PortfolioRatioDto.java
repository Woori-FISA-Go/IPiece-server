package com.masterpiece.IPiece.mypage.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioRatioDto {

    private String productName;
    private Double ratio;  // 비중 (%)
    private String thumbnailImg;
}