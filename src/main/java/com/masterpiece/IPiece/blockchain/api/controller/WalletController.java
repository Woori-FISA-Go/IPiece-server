package com.masterpiece.IPiece.blockchain.api.controller;

import com.masterpiece.IPiece.blockchain.api.dto.request.KrwtBurnRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.KrwtMintRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.TransactionQueryRequest;
import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBurnResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtMintResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.MyWalletResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TransactionListResponse;
import com.masterpiece.IPiece.blockchain.application.WalletService;
import com.masterpiece.IPiece.common.web.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('USER')")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<TransactionListResponse> getTransactions(
            @CurrentUser Long userId,
            @ModelAttribute TransactionQueryRequest request
    ) {
        TransactionListResponse response = walletService.getTransactions(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/krwt/mint")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<KrwtMintResponse> mintKrwt(
            @CurrentUser Long adminUserId,
            @Valid @RequestBody KrwtMintRequest request
    ) {
        KrwtMintResponse response = walletService.mintKrwt(adminUserId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/krwt/burn")
    @PreAuthorize("hasRole('USER')")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<KrwtBurnResponse> burnKrwt(
            @CurrentUser Long userId,
            @Valid @RequestBody KrwtBurnRequest request
    ) {
        KrwtBurnResponse response = walletService.burnKrwt(userId, request);
        return ResponseEntity.ok(response);
    }
}
