package com.masterpiece.IPiece.mypage.api.dto.response;

import com.masterpiece.IPiece.common.domain.product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferingAssetDto {
    private Long productId;
    private String productName; // 참여자산
    private String tokenName;   // 참여자산 서브
    private String thumbnailImg;    // 참여자산 썸네일

    private Long quantity;  // 참여수량
    private Long buyPrice;  // 총 구매가
    private Long offeringPrice;  // 공모가

    private Integer progressRate;   // 진행률
    private OffsetDateTime offeringStartDate;   // 공모 시작일
    private OffsetDateTime offeringEndDate;     // 공모 종료일
}
