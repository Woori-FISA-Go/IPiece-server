package com.masterpiece.IPiece.blockchain.api;

import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBalanceResponse;
import com.masterpiece.IPiece.blockchain.application.BlockchainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/blockchain/wallet")
@RequiredArgsConstructor
public class BlockchainController {

    private final BlockchainService blockchainService;

    @GetMapping("/krwt")
    public ResponseEntity<KrwtBalanceResponse> getKrwtBalance() {
        // TODO: Get userId from security context
        Long userId = 1L; // Placeholder for now
        KrwtBalanceResponse response = blockchainService.getKrwtBalance(userId);
        return ResponseEntity.ok(response);
    }
}
