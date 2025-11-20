package com.masterpiece.IPiece.offering.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 공모 상품 무한스크롤 응답 DTO
 * 
 * 특징:
 * - 페이징 정보 없음 (Page 객체 대신 List 반환)
 * - hasNext: 다음 페이지 존재 여부
 * - nextCursor: 다음 요청에 사용할 cursor (무한스크롤 위치)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferingListResponse {
    
    /** 공모 상품 리스트 */
    private List<OfferingProductResponse> items;
    
    /** 다음 페이지 존재 여부 (true = 더 불러올 상품 있음) */
    private Boolean hasNext;
    
    /** 다음 요청에 사용할 cursor 
     * (null이면 마지막 페이지) */
    private Long nextCursor;

    private Long totalCount;

    private Long beforeCount;

    private Long ingCount;

    private Long afterCount;
}
