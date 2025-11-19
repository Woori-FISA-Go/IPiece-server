package com.masterpiece.IPiece.blockchain.api;

import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBalanceResponse;
import com.masterpiece.IPiece.blockchain.application.BlockchainService;
import com.masterpiece.IPiece.common.web.annotation.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/blockchain/wallet")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = true)
public class BlockchainController {

    private final BlockchainService blockchainService;

    @GetMapping("/krwt")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<KrwtBalanceResponse> getKrwtBalance(@CurrentUser Long userId) { // Use CurrentUser
        // No need to parse userId from UserDetails anymore
        KrwtBalanceResponse response = blockchainService.getKrwtBalance(userId);
        return ResponseEntity.ok(response);
    }
}
