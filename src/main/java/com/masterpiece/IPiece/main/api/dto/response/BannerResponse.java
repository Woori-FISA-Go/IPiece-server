package com.masterpiece.IPiece.main.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerResponse {
    private Long bannerId;
    private String imageUrl;
    private String title;
    private String description;
    // banners.json의 실제 구조에 맞춰 필드 추가
}