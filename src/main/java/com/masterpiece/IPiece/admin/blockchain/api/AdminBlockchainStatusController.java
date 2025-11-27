package com.masterpiece.IPiece.admin.blockchain.api;

import com.masterpiece.IPiece.admin.blockchain.api.dto.response.AdminBlockchainStatusResponse;
import com.masterpiece.IPiece.admin.blockchain.application.AdminBlockchainStatusService;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminBlockchainStatusController {

    private final AdminBlockchainStatusService statusService;
    private final UserRepository userRepository;

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return false;
        }

        Long userId;
        try {
            userId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException e) {
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        return user != null && "admin".equals(user.getUserMadeId());
    }

    /**
     * 체인 상태 대시보드
     * GET /v1/admin/blockchain/status
     */
    @GetMapping("/v1/admin/blockchain/status")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<AdminBlockchainStatusResponse> getStatus(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).build();
        }

        try {
            AdminBlockchainStatusResponse response = statusService.getStatus();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503).build();
        }
    }
}