package com.masterpiece.IPiece.admin.blockchain.api;

import com.masterpiece.IPiece.admin.blockchain.api.dto.response.AdminBlockchainTransactionListResponse;
import com.masterpiece.IPiece.admin.blockchain.application.AdminBlockchainTransactionService;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminBlockchainTransactionController {

    private final AdminBlockchainTransactionService transactionService;
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
     * 온체인 트랜잭션 조회
     * GET /v1/admin/blockchain/transactions
     */
    @GetMapping("/v1/admin/blockchain/transactions")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<AdminBlockchainTransactionListResponse> getTransactions(
            Authentication authentication,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "tx_type", required = false) String txType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "page_size", required = false, defaultValue = "50") Integer pageSize
    ) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).build();
        }

        if (page < 1 || pageSize < 1) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize);

        AdminBlockchainTransactionListResponse response =
                transactionService.searchTransactions(userId, txType, status, productId, pageable);

        return ResponseEntity.ok(response);
    }
}