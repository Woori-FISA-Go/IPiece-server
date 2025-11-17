package com.masterpiece.IPiece.offering.api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferingListItemResponse {

    // Product 테이블 필드
    private Long productId;           // 상품 ID
    private String productName;       // 상품명 (예: "다이넷")
    private String owner;             // 발행자 (예: "위시 이름 or 저기 이름")
    private Long currentPrice;        // 현재 가격
    private String thumbnailImg;      // 썸네일 이미지 경로

    // ProductOfferingInfo 테이블 필드
    private Integer progressRate;     // 진행률 (0~100)
    private LocalDateTime offeringStartDate;  // 공모 시작일
    private LocalDateTime offeringEndDate;    // 공모 종료일
    private Long offeringPrice;       // 공모 가격 (subscriptionPrice)

    // FavoriteList (사용자 기준)
    private Boolean isFavorite;       // 찜 여부 (true/false)
}
