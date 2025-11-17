package com.masterpiece.IPiece.offering.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferingProductDetailResponse {

    // Product 테이블 필드
    private Long productId;           // 상품 ID
    private String productName;       // 상품명
    private String owner;             // 발행자
    private String thumbnailImg;      // 썸네일 이미지 경로
    private String presentImg;        // 프레젠테이션 이미지
    private String projectName;       // 프로젝트명
    private Long issueAmount;         // 발행량
    private String tokenStandard;     // 토큰 표준 (ERC-20 등)
    private String exchangeListing;   // 거래소 상장
    private Long tokenQuantity;       // 토큰 수량
    private String tokenName;         // 토큰명

    // ProductOfferingInfo 테이블 필드
    private Integer progressRate;     // 진행률 (0~100)
    private OffsetDateTime offeringStartDate;  // 공모 시작일
    private OffsetDateTime offeringEndDate;    // 공모 종료일
    private Long offeringPrice;       // 공모 가격
    private String detailImg;         // 상세 이미지
    private Long offeringAmount;      // 공모 수량

    // FavoriteList (사용자 기준)
    private Boolean isFavorite;       // 찜 여부 (true/false)
}
