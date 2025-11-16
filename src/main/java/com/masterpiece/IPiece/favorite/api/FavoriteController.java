package com.masterpiece.IPiece.favorite.api;

import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.web.Responses;
import com.masterpiece.IPiece.favorite.api.dto.request.FavoriteRegisterRequest;
import com.masterpiece.IPiece.favorite.api.dto.response.FavoriteAlreadyLikeResponse;
import com.masterpiece.IPiece.favorite.api.dto.response.FavoriteRegisterResponse;
import com.masterpiece.IPiece.favorite.api.dto.response.FavoriteUnlikeResponse;
import com.masterpiece.IPiece.favorite.application.FavoriteService;
import com.masterpiece.IPiece.favorite.application.FavoriteService.FavoriteRegisterResult;
import com.masterpiece.IPiece.favorite.application.FavoriteService.ProductNotFoundException;
import com.masterpiece.IPiece.favorite.application.FavoriteService.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;

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
     * Body:   { "product_id": "501" }
     */
    @PostMapping("/products/{product_id}/favorite")
    public ResponseEntity<?> registerFavorite(
            @AuthenticationPrincipal Long userId,           // ← MypageController와 동일
            @PathVariable("product_id") String productIdPath,
            @RequestBody FavoriteRegisterRequest request
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

            // 2. path와 body의 product_id가 다르면 400
            if (!productIdPath.equals(request.getProductId())) {
                return Responses.badRequest(
                        "https://ipiece.com/errors/" + ErrorCode.VALIDATION_ERROR.name(),
                        ErrorCode.VALIDATION_ERROR.name(),
                        "path의 product_id와 body의 product_id가 일치하지 않습니다.",
                        instanceUri
                );
            }

            // 3. 숫자(Long)로 변환
            Long productId = request.toProductIdAsLong();

            // 4. 서비스 호출
            FavoriteRegisterResult result = favoriteService.registerFavorite(userId, productId);

            String createdAtIso = result.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            // 5. 응답 분기
            if (result.isAlreadyLiked()) {
                FavoriteAlreadyLikeResponse body = FavoriteAlreadyLikeResponse.builder()
                        .status("ALREADY_LIKED")
                        .productId(request.getProductId())
                        .liked(true)
                        .build();
                return Responses.ok(body);
            } else {
                FavoriteRegisterResponse body = FavoriteRegisterResponse.builder()
                        .favoriteId(String.valueOf(result.getFavorite().getFavoriteId()))
                        .productId(request.getProductId())
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
}