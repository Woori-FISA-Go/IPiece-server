package com.masterpiece.IPiece.admin.api;

import com.masterpiece.IPiece.admin.api.dto.request.AdminCreateProductRequest;
import com.masterpiece.IPiece.admin.api.dto.response.AdminSimpleSuccessResponse;
import com.masterpiece.IPiece.admin.application.AdminProductService;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
}