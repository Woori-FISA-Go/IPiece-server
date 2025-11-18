package com.masterpiece.IPiece.blockchain.application;

import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBalanceResponse;
import com.masterpiece.IPiece.integration.besu.BesuClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockchainService {

    private final BesuClient besuClient;

    public KrwtBalanceResponse getKrwtBalance(Long userId) {
        // TODO: Get wallet address for userId
        String walletAddress = "0x..."; // Placeholder for user's wallet address

        // Interact with BesuClient to get KRWT balance
        BigDecimal balance = besuClient.getKrwtBalance(walletAddress);

        return KrwtBalanceResponse.builder()
                .balance(balance)
                .build();
    }
}
