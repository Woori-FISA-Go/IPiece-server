package com.masterpiece.IPiece.blockchain.api;

import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBalanceResponse;
import com.masterpiece.IPiece.blockchain.application.BlockchainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/blockchain/wallet")
@RequiredArgsConstructor
public class BlockchainController {

    private final BlockchainService blockchainService;

    @GetMapping("/krwt")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<KrwtBalanceResponse> getKrwtBalance(@AuthenticationPrincipal Long userId) {
        KrwtBalanceResponse response = blockchainService.getKrwtBalance(userId);
        return ResponseEntity.ok(response);
    }
}
