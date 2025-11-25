package com.masterpiece.IPiece.blockchain.api;

import com.masterpiece.IPiece.blockchain.api.dto.request.CreateTokenRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.TokenTransferRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.WhitelistRequest;
import com.masterpiece.IPiece.blockchain.api.dto.response.CreateTokenResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBalanceResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TokenInfoResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TokenTransferResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TransactionInfoResponse;
import com.masterpiece.IPiece.blockchain.application.BlockchainService;
import com.masterpiece.IPiece.common.web.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/blockchain")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = true)
public class BlockchainController {

    private final BlockchainService blockchainService;

    @GetMapping("/wallet/krwt")
    @PreAuthorize("hasRole('USER')")
    @Operation(security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<KrwtBalanceResponse> getKrwtBalance(@CurrentUser Long userId) {
        KrwtBalanceResponse response = blockchainService.getKrwtBalance(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tokens")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "프로젝트/토큰 생성", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<CreateTokenResponse> createToken(@Valid @RequestBody CreateTokenRequest request,
                                                           @CurrentUser Long adminUserId) {
        CreateTokenResponse response = blockchainService.createToken(request, adminUserId);
        return ResponseEntity.created(URI.create("/v1/blockchain/tokens/" + response.getContractAddress()))
                .body(response);
    }

    @GetMapping("/tokens/{address}")
    @Operation(summary = "토큰 정보 조회")
    public ResponseEntity<TokenInfoResponse> getTokenInfo(@PathVariable String address) {
        TokenInfoResponse response = blockchainService.getTokenInfo(address);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tokens/{address}/whitelist")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "화이트리스트 추가", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<Void> addToWhitelist(@PathVariable String address,
                                               @Valid @RequestBody WhitelistRequest request) {
        blockchainService.addToWhitelist(address, request); // Return value is ignored to keep the API contract
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tokens/{address}/transfer")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "토큰 전송", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<TokenTransferResponse> transferToken(@PathVariable String address,
                                                               @Valid @RequestBody TokenTransferRequest request,
                                                               @CurrentUser Long adminUserId) {
        TokenTransferResponse response = blockchainService.transferToken(address, request, adminUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/{hash}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "트랜잭션 조회", security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<TransactionInfoResponse> getTransactionByHash(@PathVariable String hash) {
        TransactionInfoResponse response = blockchainService.getTransactionByHash(hash);
        return ResponseEntity.ok(response);
    }
}
