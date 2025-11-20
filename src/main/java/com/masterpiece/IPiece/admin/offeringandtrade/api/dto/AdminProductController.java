package com.masterpiece.IPiece.admin.offeringandtrade.api.dto;

import com.masterpiece.IPiece.admin.offeringandtrade.api.dto.request.AdminCreateProductRequest;
import com.masterpiece.IPiece.admin.offeringandtrade.api.dto.request.AdminEnableSecondaryTradingRequest;
import com.masterpiece.IPiece.admin.offeringandtrade.api.dto.response.AdminEnableSecondaryTradingResponse;
import com.masterpiece.IPiece.admin.offeringandtrade.api.dto.response.AdminSimpleSuccessResponse;
import com.masterpiece.IPiece.admin.offeringandtrade.application.AdminProductService;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;
    private final UserRepository userRepository;

    @PostMapping("/v1/admin/products")
    public ResponseEntity<AdminSimpleSuccessResponse> createProduct(
            Authentication authentication,
            @Valid @RequestBody AdminCreateProductRequest request
    ) {
        // 1. 인증 체크
        if (authentication == null || authentication.getName() == null) {
            // JWT 필터에서 이미 401을 줄 수도 있지만, 여기서도 한 번 더 방어
            return ResponseEntity.status(401).build();
        }

        // 2. 토큰 subject 를 userId 로 가정하고 DB에서 유저 조회
        Long userId;
        try {
            userId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            // subject 가 숫자가 아니면 잘못된 토큰 구조라고 보고 401
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null || !"admin".equals(user.getUserMadeId())) {
            // 유저가 없거나, user_made_id 가 "admin" 이 아니면 관리자 아님 → 403
            return ResponseEntity.status(403).build();
        }

        // 3. 서비스 호출
        adminProductService.createProductWithOffering(request);

        // 4. 성공 응답
        return ResponseEntity.ok(new AdminSimpleSuccessResponse(true));
    }

    /**
     * 공모(OFFERING) 상품에 대해 2차거래(TRADE) 시작 승인
     *
     * POST /v1/admin/products/{productId}/enable-offering
     */
    @PostMapping("/v1/admin/products/{productId}/enable-offering")
    @PreAuthorize("hasRole('ADMIN')") // 실제 권한 이름에 맞게 조정 필요
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<AdminEnableSecondaryTradingResponse> enableSecondaryTrading(
            @AuthenticationPrincipal Long operatorId,                  // JWT에서 온 user_id
            @PathVariable("productId") Long productId,
            @Valid @RequestBody AdminEnableSecondaryTradingRequest request
    ) {
        AdminEnableSecondaryTradingResponse response =
                adminProductService.enableSecondaryTrading(productId, operatorId, request);

        return ResponseEntity.ok(response);
    }
}