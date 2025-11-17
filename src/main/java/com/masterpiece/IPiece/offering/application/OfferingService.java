package com.masterpiece.IPiece.offering.application;

import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.product.ProductStatus;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.market.application.port.FavoriteQueryPort;
import com.masterpiece.IPiece.offering.api.dto.response.OfferingListResponse;
import com.masterpiece.IPiece.offering.api.dto.response.OfferingProductResponse;
import com.masterpiece.IPiece.offering.domain.ProductOfferingInfo;
import com.masterpiece.IPiece.offering.infra.ProductOfferingInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OfferingService
 * 
 * 무한스크롤 방식:
 * - Cursor 기반 (productId 기준)
 * - 첫 요청: cursor = null
 * - 다음 요청: cursor = 이전 마지막 상품의 productId - 1
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OfferingService {

    private final ProductRepository productRepository;
    private final ProductOfferingInfoRepository productOfferingInfoRepository;
    private final FavoriteQueryPort favoriteQueryPort;

    /** 무한스크롤 페이지 사이즈 (한 번에 로드할 항목 수) */
    private static final int PAGE_SIZE = 12;

    /**
     * 공모중 상품 무한스크롤 조회
     * 
     * 동작:
     * 1) cursor 기반으로 상품 조회 (productId 기준 DESC)
     * 2) PAGE_SIZE + 1개 조회 (hasNext 판단용)
     * 3) 공모 정보, 찜 정보 배치 조회
     * 4) Response 생성 (hasNext, nextCursor 포함)
     * 
     * @param cursor 마지막 상품의 productId (첫 요청 시 null)
     * @param userId 로그인 사용자 ID (null이면 비로그인)
     * @return 무한스크롤 응답 (items, hasNext, nextCursor)
     */
    public OfferingListResponse getOfferingProductsInfinite(Long cursor, Long userId) {
        
        // 커서 기반 상품 조회
        // hasNext 판단을 위해 PAGE_SIZE + 1개 조회
        List<Product> products = fetchProducts(cursor, PAGE_SIZE + 1);

        // 데이터가 없으면 빈 결과 반환
        if (products.isEmpty()) {
            return OfferingListResponse.builder()
                    .items(List.of())
                    .hasNext(false)
                    .nextCursor(null)
                    .build();
        }

        // hasNext 판단 : 13개 조회되면 true
        boolean hasNext = products.size() > PAGE_SIZE;

        // 실제로 반환할 데이터는 PAGE_SIZE만(12개)
        List<Product> itemsToReturn = hasNext 
                ? products.subList(0, PAGE_SIZE)  // PAGE_SIZE개만 반환
                : products;                        // 모두 반환

        // productIds 추출
        List<Long> productIds = itemsToReturn.stream()
                .map(Product::getProductId)
                .collect(Collectors.toList());

        // 공모 정보 배치 조회
        // 배치 조회란??? 상품마다 공모정보를 조회하는게 아니라 상품Id들을 한번에 조회하는 방식!
        List<ProductOfferingInfo> offeringInfos = productOfferingInfoRepository
                .findByProductIdIn(productIds);


        Map<Long, ProductOfferingInfo> offeringInfoMap = offeringInfos.stream()
                .collect(Collectors.toMap(
                        ProductOfferingInfo::getProductId,
                        info -> info,
                        (existing, replacement) -> existing
                ));

        // 찜 정보 배치 조회 (userId가 null이면 빈 Set반환)
        Set<Long> favoritedProductIds = favoriteQueryPort.findProductIdsByUserId(userId);

        // product + 공모정보 + 찜여부 -> ResponseDTO 변환
        List<OfferingProductResponse> responses = itemsToReturn.stream()
                .map(product -> convertToOfferingProductResponse(
                        product,
                        offeringInfoMap.get(product.getProductId()),
                        favoritedProductIds.contains(product.getProductId())
                ))
                .collect(Collectors.toList());

        // nextCursor 계산
        // (마지막 상품의 productId - 1로 설정)
        Long nextCursor = hasNext 
                ? itemsToReturn.get(itemsToReturn.size() - 1).getProductId() - 1
                : null;


        return OfferingListResponse.builder()
                .items(responses)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    /**
     * 공모 상품 상세 조회 (기존 로직)
     */
    public OfferingProductResponse getOfferingProductDetail(Long productId, Long userId) {
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() != ProductStatus.OFFERING) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_OFFERING);
        }

        ProductOfferingInfo offeringInfo = productOfferingInfoRepository
                .findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_INFO_NOT_FOUND));

        boolean isFavorited = favoriteQueryPort.existsByUserIdAndProductId(userId, productId);

        return convertToOfferingProductResponse(product, offeringInfo, isFavorited);
    }

    /**
     * 커서 기반 상품 조회
     * 
     * @param cursor 마지막 상품의 productId (null이면 첫 로드)
     * @param limit 조회할 항목 수 + 1 (hasNext 판단용)
     * @return 상품 리스트
     */
    private List<Product> fetchProducts(Long cursor, int limit) {
        List<Product> products;
        
        if (cursor == null) {
            // 첫 로드: cursor 없음
            products = productRepository.findByStatusInitial(ProductStatus.OFFERING, limit);
        } else {
            // 다음 로드: cursor 기반
            products = productRepository.findByStatusCursorBased(
                    ProductStatus.OFFERING,
                    cursor,
                    limit
            );
        }

        // Java에서 LIMIT 적용 (JPA가 지원하지 않아서)
        return products.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Product + ProductOfferingInfo → OfferingProductResponse 변환
     */
    private OfferingProductResponse convertToOfferingProductResponse(
            Product product,
            ProductOfferingInfo offeringInfo,
            boolean isFavorite
    ) {
        return OfferingProductResponse.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .owner(product.getOwner())
                .thumbnailImg(product.getThumbnailImg())
                .progressRate(offeringInfo.getProgressRate())
                .offeringStartDate(offeringInfo.getOfferingStartDate())
                .offeringEndDate(offeringInfo.getOfferingEndDate())
                .offeringPrice(offeringInfo.getOfferingPrice())
                .isFavorite(isFavorite)
                .build();
    }
}
