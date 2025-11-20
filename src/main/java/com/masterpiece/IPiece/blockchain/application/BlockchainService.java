package com.masterpiece.IPiece.blockchain.application;

import com.masterpiece.IPiece.blockchain.api.dto.request.CreateTokenRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.WhitelistRequest;
import com.masterpiece.IPiece.blockchain.api.dto.response.CreateTokenResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBalanceResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TokenInfoResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TokenListResponse;
import com.masterpiece.IPiece.blockchain.domain.BlockchainToken;
import com.masterpiece.IPiece.blockchain.domain.TokenStatus;
import com.masterpiece.IPiece.blockchain.infra.jpa.BlockchainTokenRepository;
import com.masterpiece.IPiece.blockchain.infra.jpa.WalletRepository;
import com.masterpiece.IPiece.common.exception.BlockchainException;
import com.masterpiece.IPiece.common.exception.TokenNotFoundException;
import com.masterpiece.IPiece.common.exception.WalletNotFoundException;
import com.masterpiece.IPiece.integration.besu.BesuClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = true)
public class BlockchainService {

    private final BesuClient besuClient;
    private final WalletRepository walletRepository;
    private final BlockchainTokenRepository blockchainTokenRepository;

    public KrwtBalanceResponse getKrwtBalance(Long userId) {
        String walletAddress = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("User " + userId + " has no wallet"))
                .getAddress();

        try {
            BigDecimal balance = besuClient.getKrwtBalance(walletAddress);
            return KrwtBalanceResponse.builder()
                    .balance(balance)
                    .build();
        } catch (Exception e) {
            throw new BlockchainException("Failed to fetch KRWT balance for wallet: " + walletAddress, e);
        }
    }

    @Transactional
    public CreateTokenResponse createToken(CreateTokenRequest request, Long adminUserId) {
        // For now, we'll use dummy values for contract deployment
        String dummyContractAddress = "0x" + UUID.randomUUID().toString().replace("-", "");
        String dummyTransactionHash = "0x" + UUID.randomUUID().toString().replace("-", "");

        BlockchainToken token = BlockchainToken.builder()
                .name(request.getName())
                .symbol(request.getSymbol())
                .totalSupply(request.getTotalSupply())
                .faceValue(request.getFaceValue())
                .ownerUserId(adminUserId)
                .contractAddress(dummyContractAddress)
                .transactionHash(dummyTransactionHash)
                .status(TokenStatus.DEPLOYED) // Set directly to DEPLOYED as per review
                .build();

        BlockchainToken savedToken = blockchainTokenRepository.save(token);

        return CreateTokenResponse.builder()
                .contractAddress(savedToken.getContractAddress())
                .transactionHash(savedToken.getTransactionHash())
                .name(savedToken.getName())
                .symbol(savedToken.getSymbol())
                .totalSupply(savedToken.getTotalSupply())
                .faceValue(savedToken.getFaceValue())
                .createdAt(savedToken.getCreatedAt())
                .build();
    }

    public TokenInfoResponse getTokenInfo(String contractAddress) {
        BlockchainToken token = blockchainTokenRepository.findByContractAddress(contractAddress)
                .orElseThrow(() -> new TokenNotFoundException("Token with address " + contractAddress + " not found"));

        return TokenInfoResponse.from(token);
    }

    public TokenListResponse getTokenList(Pageable pageable) {
        Page<BlockchainToken> tokenPage = blockchainTokenRepository.findAll(pageable);
        Page<TokenInfoResponse> tokenInfoPage = tokenPage.map(TokenInfoResponse::from);
        return new TokenListResponse(tokenInfoPage);
    }

    @Transactional
    public void addToWhitelist(String contractAddress, WhitelistRequest request) {
        log.info("Attempting to add address {} to whitelist for contract {}", request.getUserWalletAddress(), contractAddress);

        // 1. Find the token contract info from our DB
        BlockchainToken token = blockchainTokenRepository.findByContractAddress(contractAddress)
                .orElseThrow(() -> new TokenNotFoundException("Token with address " + contractAddress + " not found"));

        // 2. Call BesuClient to execute the smart contract function
        try {
            // This is a mocked call for now. In a real scenario, this would interact with the blockchain.
            besuClient.addToWhitelist(token.getContractAddress(), request.getUserWalletAddress());
            log.info("Successfully added address {} to whitelist for contract {}", request.getUserWalletAddress(), contractAddress);
        } catch (Exception e) {
            log.error("Failed to add address to whitelist for contract {}", contractAddress, e);
            throw new BlockchainException("Failed to add to whitelist for contract: " + contractAddress, e);
        }
    }
}
