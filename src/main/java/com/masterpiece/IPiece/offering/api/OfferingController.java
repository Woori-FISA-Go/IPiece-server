package com.masterpiece.IPiece.offering.api;

import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.util.JwtTokenProvider;
import com.masterpiece.IPiece.common.web.Responses;
import com.masterpiece.IPiece.offering.api.dto.response.OfferingListResponse;
import com.masterpiece.IPiece.offering.api.dto.response.OfferingProductResponse;
import com.masterpiece.IPiece.offering.application.OfferingService;
import com.masterpiece.IPiece.user.infra.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OfferingController
 * 
 * API:
 * - GET /v1/offerings → 무한스크롤 리스트
 * - GET /v1/offerings/{productId} → 상세 조회
 */
@Slf4j
@RestController
@RequestMapping("/v1/offerings")
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService offeringService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 공모중 상품 무한스크롤 조회
     * 
     * HTTP: GET /v1/offerings
     * 
     * 쿼리 파라미터:
     * - cursor: 마지막 상품의 productId (첫 요청 시 생략 또는 null)
     *   → 다음 요청부터 응답의 nextCursor 사용
     * 
     * 헤더:
     * - Authorization: Bearer {accessToken} (선택사항)
     * 
     * 응답: OfferingListResponse
     * {
     *   "items": [상품 배열],
     *   "hasNext": true/false,
     *   "nextCursor": 123 (또는 null if 마지막 페이지)
     * }
     */
    @GetMapping
    public ResponseEntity<?> getOfferingProducts(
            @RequestParam(value = "cursor", required = false) Long cursor,
            HttpServletRequest request
    ) {
        // Authorization 헤더에서 userId 추출
        Long userId = extractUserIdFromToken(request);

        log.debug("공모 상품 무한스크롤 조회 - cursor: {}, userId: {}", cursor, userId);

        // OfferingService 호출
        OfferingListResponse response = offeringService.getOfferingProductsInfinite(cursor, userId);

        // 응답 반환
        return Responses.ok(response);
    }

    /**
     * 공모 상품 상세 조회
     * 
     * HTTP: GET /v1/offerings/{productId}
     * 
     * 경로 파라미터:
     * - productId: 상품 ID
     * 
     * 헤더:
     * - Authorization: Bearer {accessToken} (선택사항)
     */
    @GetMapping("/detail/{productId}")
    public ResponseEntity<?> getOfferingProductDetail(
            @PathVariable Long productId,
            HttpServletRequest request
    ) {
        if (productId == null || productId <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        Long userId = extractUserIdFromToken(request);

        OfferingProductResponse response = offeringService.getOfferingProductDetail(productId, userId);

        return Responses.ok(response);
    }

    /**
     * Authorization 헤더에서 userId 추출 (null-safe)
     */
    private Long extractUserIdFromToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }

            String token = authHeader.substring(7);
            
            ErrorCode error = jwtTokenProvider.validateToken(token);
            if (error != null) {
                log.warn("JWT 검증 실패 - error: {}", error);
                return null;
            }

            String userIdStr = jwtTokenProvider.getSubject(token);
            Long userId = Long.valueOf(userIdStr);

            if (!userRepository.existsById(userId)) {
                log.warn("사용자를 찾을 수 없음 - userId: {}", userId);
                return null;
            }

            return userId;
        } catch (Exception e) {
            log.warn("토큰 파싱 중 에러 발생", e);
            return null;
        }
    }
}
