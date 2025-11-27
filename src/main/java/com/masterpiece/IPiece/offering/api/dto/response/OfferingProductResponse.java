package com.masterpiece.IPiece.offering.api.dto.response;


import com.masterpiece.IPiece.common.domain.product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferingProductResponse {

    // Product 테이블 필드
    private Long productId;           // 상품 ID
    private String productName;       // 상품명
    private String owner;             // 발행자
    private String thumbnailImg;      // 썸네일 이미지 경로
    private String projectName;       // 프로젝트 이름
    private ProductStatus status;     // 상태값

    // ProductOfferingInfo 테이블 필드
    private Integer progressRate;     // 진행률 (0~100)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private OffsetDateTime offeringStartDate;  // 공모 시작일
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private OffsetDateTime offeringEndDate;    // 공모 종료일
    private Long offeringPrice;       // 공모 가격
    private Long offeringAmount;    // 공모 수량

    // FavoriteList (사용자 기준)
    private Boolean isFavorite;       // 찜 여부 (true/false)
}
