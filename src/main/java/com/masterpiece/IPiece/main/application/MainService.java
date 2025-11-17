package com.masterpiece.IPiece.main.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.product.ProductStatus;
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.main.api.dto.response.MainPageResponse;
import com.masterpiece.IPiece.main.api.dto.response.ProductCardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    /**
     * 메인 페이지 데이터 조회
     * - 배너 (resources/banners.json 에서 읽음)
     * - 공모중 상품 (최신순, 4개)
     * - 거래중 상품 (최신순, 4개)
     */
    public MainPageResponse getMainPage() {
        // 1. 배너 로드 (JSON 파일에서)
        List<Map<String, Object>> banners = loadBannersFromJson();

        // 2. 공모중 상품 (최신순, 4개)
        Pageable offeringPageable = PageRequest.of(0, 4, 
                Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ProductCardResponse> offeringProducts = productRepository
                .findByStatus(ProductStatus.OFFERING, offeringPageable)
                .stream()
                .map(this::convertToProductCardResponse)
                .collect(Collectors.toList());

        // 3. 거래중 상품 (최신순, 4개)
        Pageable tradingPageable = PageRequest.of(0, 4, 
                Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ProductCardResponse> tradingProducts = productRepository
                .findByStatus(ProductStatus.TRADE, tradingPageable)
                .stream()
                .map(this::convertToProductCardResponse)
                .collect(Collectors.toList());

        return MainPageResponse.builder()
                .banners(banners)
                .offeringProducts(offeringProducts)
                .tradingProducts(tradingProducts)
                .build();
    }

    /**
     * resources/banners.json 파일에서 배너 데이터 로드
     */
    private List<Map<String, Object>> loadBannersFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("banners.json");
            InputStream inputStream = resource.getInputStream();
            return objectMapper.readValue(inputStream, 
                    new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.error("배너 파일 읽기 실패", e);
            return List.of();  // 빈 리스트 반환
        }
    }

    /**
     * Product → ProductCardDto 변환
     * (필요한 필드만 매핑)
     */
    private ProductCardResponse convertToProductCardResponse(Product product) {
        return ProductCardResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())        // "다이넷"
                .currentPrice(product.getCurrentPrice())      // 200
                .thumbnailImg(product.getThumbnailImg())      // 이미지 경로
                .build();
    }
}
