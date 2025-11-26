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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;
    private final UserRepository userRepository;

    @PostMapping(
            value = "/v1/admin/products",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<AdminSimpleSuccessResponse> createProduct(
            Authentication authentication,
            @Valid @RequestPart("data") AdminCreateProductRequest request,
            @RequestPart(value = "presentImg", required = false) MultipartFile presentImg,
            @RequestPart(value = "thumbnailImg", required = false) MultipartFile thumbnailImg,
            @RequestPart(value = "detailImg", required = false) MultipartFile detailImg
    ) {
        // 1. 인증 체크
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        // 2. 토큰 subject 를 userId 로 가정하고 DB에서 유저 조회
        Long userId;
        try {
            userId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null || !"admin".equals(user.getUserMadeId())) {
            return ResponseEntity.status(403).build();
        }

        // 3. 서비스 호출 (이미지 포함)
        adminProductService.createProductWithOffering(request, presentImg, thumbnailImg, detailImg);

        // 4. 성공 응답
        return ResponseEntity.ok(new AdminSimpleSuccessResponse(true));
    }

    /**
     * 공모(OFFERING) 상품에 대해 2차거래(TRADE) 시작 승인
     * <p>
     * POST /v1/admin/products/{productId}/enable-offering
     */
    @PostMapping("/v1/admin/products/{productId}/enable-offering")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<AdminEnableSecondaryTradingResponse> enableSecondaryTrading(
            Authentication authentication,
            @PathVariable("productId") Long productId,
            @Valid @RequestPart("data") AdminEnableSecondaryTradingRequest request
    ) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        Long userId;
        try {
            userId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user == null || !"admin".equals(user.getUserMadeId())) {
            return ResponseEntity.status(403).build();
        }

        AdminEnableSecondaryTradingResponse response =
                adminProductService.enableSecondaryTrading(productId, request);

        return ResponseEntity.ok(response);
    }
}