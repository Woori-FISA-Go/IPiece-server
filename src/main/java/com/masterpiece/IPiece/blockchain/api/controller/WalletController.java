package com.masterpiece.IPiece.blockchain.api.controller;

import com.masterpiece.IPiece.blockchain.api.dto.response.MyWalletResponse;
import com.masterpiece.IPiece.blockchain.application.WalletService;
import com.masterpiece.IPiece.common.web.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

 @RestController @RequestMapping("/v1/blockchain/wallet")
 @RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<MyWalletResponse> getMyWalletInfo(
        @CurrentUser Long userId // Use the new custom annotation
    ) {
        // No need to parse userId from UserDetails anymore
        // Service 호출
        MyWalletResponse response = walletService.getMyWallet(userId);
        
        return ResponseEntity.ok(response);
    }
}
