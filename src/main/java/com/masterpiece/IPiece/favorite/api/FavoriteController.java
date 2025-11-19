package com.masterpiece.IPiece.favorite.api;

import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.web.Responses;
import com.masterpiece.IPiece.favorite.api.dto.request.FavoriteBatchStatusRequest;
import com.masterpiece.IPiece.favorite.api.dto.response.*;
import com.masterpiece.IPiece.favorite.application.FavoriteService;
import com.masterpiece.IPiece.favorite.application.FavoriteService.FavoriteRegisterResult;
import com.masterpiece.IPiece.favorite.application.FavoriteService.ProductNotFoundException;
import com.masterpiece.IPiece.favorite.application.FavoriteService.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 즐겨찾기 등록
     *
     * POST /v1/products/{product_id}/favorite
     * Header: Authorization: Bearer {accessToken}
     */
    @PostMapping("/products/{product_id}/favorite")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<?> registerFavorite(
            @AuthenticationPrincipal Long userId,           // ← MypageController와 동일
            @PathVariable("product_id") String productIdPath
    ) {
        String instanceUri = "/v1/products/" + productIdPath + "/favorite";

        try {
            // 1. 인증 여부 확인 (SecurityConfig에서 이미 걸러지지만 방어적으로 한 번 더 확인)
            if (userId == null) {
                return Responses.unauthorized(
                        "https://ipiece.com/errors/" + ErrorCode.AUTH_REQUIRED.name(),
                        ErrorCode.AUTH_REQUIRED.name(),
                        ErrorCode.AUTH_REQUIRED.getMessage(),
                        instanceUri
                );
            }

            // 2. path의 product_id를 Long으로 변환
            Long productId;
            try {
                productId = Long.parseLong(productIdPath);
            } catch (NumberFormatException e) {
                return Responses.badRequest(
                        "https://ipiece.com/errors/" + ErrorCode.VALIDATION_ERROR.name(),
                        ErrorCode.VALIDATION_ERROR.name(),
                        "product_id는 양의 정수여야 합니다.",
                        instanceUri
                );
            }

            // 3. 서비스 호출
            FavoriteRegisterResult result = favoriteService.registerFavorite(userId, productId);

            String createdAtIso = result.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            // 5. 응답 분기
            if (result.isAlreadyLiked()) {
                FavoriteAlreadyLikeResponse body = FavoriteAlreadyLikeResponse.builder()
                        .status("ALREADY_LIKED")
                        .productId(productIdPath)
                        .liked(true)
                        .build();
                return Responses.ok(body);
            } else {
                FavoriteRegisterResponse body = FavoriteRegisterResponse.builder()
                        .favoriteId(String.valueOf(result.getFavorite().getFavoriteId()))
                        .productId(productIdPath)
                        .liked(true)
                        .createdAt(createdAtIso)
                        .build();
                return Responses.ok(body);
            }

        } catch (ProductNotFoundException e) {
            return Responses.notFound(
                    "https://ipiece.com/errors/" + ErrorCode.NOT_FOUND.name(),
                    ErrorCode.NOT_FOUND.name(),
                    "상품을 찾을 수 없습니다.",
                    instanceUri
            );
        } catch (UserNotFoundException e) {
            return Responses.notFound(
                    "https://ipiece.com/errors/" + ErrorCode.NOT_FOUND.name(),
                    ErrorCode.NOT_FOUND.name(),
                    "사용자를 찾을 수 없습니다.",
                    instanceUri
            );
        } catch (Exception e) {
            return Responses.internalError(
                    "https://ipiece.com/errors/" + ErrorCode.INTERNAL_ERROR.name(),
                    ErrorCode.INTERNAL_ERROR.name(),
                    ErrorCode.INTERNAL_ERROR.getMessage(),
                    instanceUri
            );
        }
    }

    /**
     * 즐겨찾기 해제
     *
     * DELETE /v1/products/{product_id}/favorite
     * Header: Authorization: Bearer {accessToken}
     */
    @DeleteMapping("/products/{product_id}/favorite")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<?> unregisterFavorite(
            @AuthenticationPrincipal Long userId,
            @PathVariable("product_id") String productIdPath
    ) {
        String instanceUri = "/v1/products/" + productIdPath + "/favorite";

        try {
            // 1. 인증 여부 확인
            if (userId == null) {
                return Responses.unauthorized(
                        "https://ipiece.com/errors/" + ErrorCode.AUTH_REQUIRED.name(),
                        ErrorCode.AUTH_REQUIRED.name(),
                        ErrorCode.AUTH_REQUIRED.getMessage(),
                        instanceUri
                );
            }

            // 2. path의 product_id를 Long으로 변환
            Long productId = Long.parseLong(productIdPath);

            // 3. 서비스 호출 (즐겨찾기 해제)
            FavoriteService.FavoriteUnlikeResult result =
                    favoriteService.unregisterFavorite(userId, productId);

            String updatedAtIso = result.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            // 4. 응답 DTO 조립
            FavoriteUnlikeResponse body = FavoriteUnlikeResponse.builder()
                    .status("UNLIKED")
                    .favoriteId(String.valueOf(result.getFavoriteId()))
                    .liked(false)
                    .updatedAt(updatedAtIso)
                    .build();

            return Responses.ok(body);

        } catch (FavoriteService.FavoriteNotFoundException e) {
            // 명세: favorite_id 없음 → 404
            return Responses.notFound(
                    "https://ipiece.com/errors/" + ErrorCode.NOT_FOUND.name(),
                    ErrorCode.NOT_FOUND.name(),
                    "favorite_id가 존재하지 않습니다.",
                    instanceUri
            );
        } catch (NumberFormatException e) {
            // product_id가 숫자가 아닐 경우
            return Responses.badRequest(
                    "https://ipiece.com/errors/" + ErrorCode.VALIDATION_ERROR.name(),
                    ErrorCode.VALIDATION_ERROR.name(),
                    "product_id는 양의 정수여야 합니다.",
                    instanceUri
            );
        } catch (Exception e) {
            return Responses.internalError(
                    "https://ipiece.com/errors/" + ErrorCode.INTERNAL_ERROR.name(),
                    ErrorCode.INTERNAL_ERROR.name(),
                    ErrorCode.INTERNAL_ERROR.getMessage(),
                    instanceUri
            );
        }
    }

    /**
     * 렌더용 배치 조회
     *
     * POST /v1/favorites/status
     * Body: { "product_ids": ["1","2","3"] }
     */
    @PostMapping("/favorites/status")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<?> getFavoriteStatusBatch(
            @AuthenticationPrincipal Long userId,
            @RequestBody FavoriteBatchStatusRequest request
    ) {
        String instanceUri = "/v1/favorites/status";

        try {
            // 1. 인증 여부 확인
            if (userId == null) {
                return Responses.unauthorized(
                        "https://ipiece.com/errors/" + ErrorCode.AUTH_REQUIRED.name(),
                        ErrorCode.AUTH_REQUIRED.name(),
                        ErrorCode.AUTH_REQUIRED.getMessage(),
                        instanceUri
                );
            }

            // 2. 기본 검증: null/개수 제한
            if (request == null || request.isEmpty()) {
                return Responses.badRequest(
                        "https://ipiece.com/errors/" + ErrorCode.VALIDATION_ERROR.name(),
                        ErrorCode.VALIDATION_ERROR.name(),
                        "product_ids는 최소 1개 이상이어야 합니다.",
                        instanceUri
                );
            }

            int size = request.size();
            if (size < 1 || size > 100) {
                return Responses.badRequest(
                        "https://ipiece.com/errors/" + ErrorCode.VALIDATION_ERROR.name(),
                        ErrorCode.VALIDATION_ERROR.name(),
                        "product_ids는 1~100개여야 합니다.",
                        instanceUri
                );
            }

            // 3. 문자열 product_id들을 Long 리스트로 변환
            List<Long> productIds;
            try {
                productIds = request.toProductIdLongList();
            } catch (NumberFormatException e) {
                return Responses.badRequest(
                        "https://ipiece.com/errors/" + ErrorCode.VALIDATION_ERROR.name(),
                        ErrorCode.VALIDATION_ERROR.name(),
                        "product_ids 항목은 숫자 문자열이어야 합니다.",
                        instanceUri
                );
            }

            // 4. 서비스 호출
            List<FavoriteService.FavoriteStatusItem> statusItems =
                    favoriteService.getFavoriteStatusBatch(userId, productIds);

            // 5. 응답 DTO 변환 (문자열 product_id로 다시 변환)
            Map<Long, String> originalIdMap = new java.util.HashMap<>();
            for (String pidStr : request.getProductIds()) {
                Long pid = Long.parseLong(pidStr);
                originalIdMap.put(pid, pidStr);
            }

            List<FavoriteStatusItemResponse> resultItems = statusItems.stream()
                    .map(item -> FavoriteStatusItemResponse.builder()
                            .productId(originalIdMap.getOrDefault(item.getProductId(),
                                    String.valueOf(item.getProductId())))
                            .liked(item.isLiked())
                            .favoriteId(item.isLiked()
                                    ? String.valueOf(item.getFavoriteId())
                                    : null)
                            .build())
                    .toList();

            FavoriteBatchStatusResponse body = FavoriteBatchStatusResponse.builder()
                    .results(resultItems)
                    .build();

            return Responses.ok(body);

        } catch (Exception e) {
            return Responses.internalError(
                    "https://ipiece.com/errors/" + ErrorCode.INTERNAL_ERROR.name(),
                    ErrorCode.INTERNAL_ERROR.name(),
                    ErrorCode.INTERNAL_ERROR.getMessage(),
                    instanceUri
            );
        }
    }
}
