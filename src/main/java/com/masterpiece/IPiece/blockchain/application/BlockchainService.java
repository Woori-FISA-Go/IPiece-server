package com.masterpiece.IPiece.blockchain.application;

import com.masterpiece.IPiece.blockchain.api.dto.request.CreateTokenRequest;
import com.masterpiece.IPiece.user.infra.UserRepository;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.blockchain.api.dto.request.TokenTransferRequest;
import com.masterpiece.IPiece.blockchain.api.dto.request.WhitelistRequest;
import com.masterpiece.IPiece.blockchain.api.dto.response.CreateTokenResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.KrwtBalanceResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TokenInfoResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TokenListResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TokenTransferResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TransactionInfoResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.ContractInfoResponse;
import com.masterpiece.IPiece.blockchain.domain.BlockchainToken;
import com.masterpiece.IPiece.blockchain.domain.BlockchainTransaction;
import com.masterpiece.IPiece.blockchain.domain.TokenStatus;
import com.masterpiece.IPiece.blockchain.domain.TransactionStatus;
import com.masterpiece.IPiece.blockchain.domain.TransactionType;
import com.masterpiece.IPiece.blockchain.infra.jpa.BlockchainTokenRepository;
import com.masterpiece.IPiece.blockchain.infra.jpa.BlockchainTransactionRepository;
import com.masterpiece.IPiece.blockchain.infra.jpa.WalletRepository;
import com.masterpiece.IPiece.common.exception.BlockchainException;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.exception.TokenNotFoundException;
import com.masterpiece.IPiece.common.exception.WalletNotFoundException;
import com.masterpiece.IPiece.integration.besu.BesuClient;
import com.masterpiece.IPiece.investment.domain.Investment;
import com.masterpiece.IPiece.investment.infra.jpa.InvestmentRepository;
import com.masterpiece.IPiece.market.domain.OrderBook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
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
    private final BlockchainTransactionRepository blockchainTransactionRepository; // Inject repository
    private final UserRepository userRepository;
    private final InvestmentRepository investmentRepository;


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
        String dummyContractAddress = besuClient.randomAddress();
        String dummyTransactionHash = besuClient.randomHash();

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
    public String addToWhitelist(String contractAddress, WhitelistRequest request) {
        log.info("Attempting to add address {} to whitelist for contract {}", request.getUserWalletAddress(), contractAddress);

        // 1. Find the token contract info from our DB
        BlockchainToken token = blockchainTokenRepository.findByContractAddress(contractAddress)
                .orElseThrow(() -> new TokenNotFoundException("Token with address " + contractAddress + " not found"));

        // 2. Call BesuClient to execute the smart contract function
        try {
            // This is a mocked call for now. In a real scenario, this would interact with the blockchain.
            String txHash = besuClient.addToWhitelist(token.getContractAddress(), request.getUserWalletAddress());
            log.info("Successfully added address {} to whitelist for contract {}", request.getUserWalletAddress(), contractAddress);
            return txHash;
        } catch (Exception e) {
            log.error("Failed to add address to whitelist for contract {}", contractAddress, e);
            throw new BlockchainException("Failed to add to whitelist for contract: " + contractAddress, e);
        }
    }

    public boolean isWhitelisted(String contractAddress, String userWalletAddress) {
        return besuClient.isWhitelisted(contractAddress, userWalletAddress);
    }

    @Transactional
    public TokenTransferResponse transferToken(String contractAddress, TokenTransferRequest request, Long adminUserId) {
        // 1. Find the token contract info from our DB
        BlockchainToken token = blockchainTokenRepository.findByContractAddress(contractAddress)
                .orElseThrow(() -> new TokenNotFoundException("Token with address " + contractAddress + " not found"));

        // 2. Admin User 조회 (user_id NOT NULL 을 만족시키기 위해)
        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new WalletNotFoundException("Admin user " + adminUserId + " not found"));

        // 3. Find the investment
        Investment investment = investmentRepository.findById(request.getInvestmentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Investment with ID " + request.getInvestmentId() + " not found"));


        // 4. Call BesuClient to execute the smart contract function
        try {
            // This is a mocked call for now. In a real scenario, this would interact with the blockchain.
            String transactionHash = besuClient.transferToken(token.getContractAddress(), request.getToAddress(), request.getAmount());
            log.info("Successfully transferred {} tokens from admin to {} for contract {}", request.getAmount(), request.getToAddress(), contractAddress);

            // Save transfer record to BlockchainTransaction entity
            BlockchainTransaction transaction = BlockchainTransaction.builder()
                    .txHash(transactionHash)
                    .fromAddress(besuClient.getAdminAddress())
                    .toAddress(request.getToAddress())
                    .tokenAddress(contractAddress)
                    .amount(BigDecimal.valueOf(request.getAmount()))
                    .transactionType(TransactionType.TRANSFER)
                    .transactionStatus(TransactionStatus.SUCCESS)
                    .user(adminUser)
                    .investment(investment)
                    .build();

            blockchainTransactionRepository.save(transaction);


            return TokenTransferResponse.builder()
                    .fromAddress(besuClient.getAdminAddress()) // Admin's wallet address
                    .toAddress(request.getToAddress())
                    .amount(request.getAmount())
                    .transactionHash(transactionHash)
                    .transferredAt(OffsetDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Failed to transfer tokens for contract {}", contractAddress, e);
            throw new BlockchainException("Failed to transfer tokens for contract: " + contractAddress, e);
        }
    }

    @Transactional(readOnly = true)
    public TransactionInfoResponse getTransactionByHash(String hash) {
        try {
            Map<String, Object> receiptData = besuClient.getTransactionReceipt(hash);

            // ✅ 각 필드 안전하게 가져오기
            String status = (String) receiptData.get("status");
            Object blockNumberObj = receiptData.get("blockNumber");
            Long blockNumber = blockNumberObj != null ?
                ((Number) blockNumberObj).longValue() : null;

            String from = (String) receiptData.get("from");
            String to = (String) receiptData.get("to");
            String value = (String) receiptData.get("value");
            String gasUsed = (String) receiptData.get("gasUsed");
            String gasPrice = (String) receiptData.get("gasPrice");

            // ✅ timestamp null 체크
            String timestampStr = (String) receiptData.get("timestamp");
            OffsetDateTime timestamp = null;
            if (timestampStr != null && !timestampStr.isEmpty()) {
                try {
                    timestamp = OffsetDateTime.parse(timestampStr);
                } catch (Exception e) {
                    log.warn("Failed to parse timestamp: {}", timestampStr, e);
                }
            }

            // ✅ logs null 체크
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawLogs = (List<Map<String, Object>>) receiptData.get("logs");
            if (rawLogs == null) {
                rawLogs = java.util.Collections.emptyList();
            }

            List<TransactionInfoResponse.Log> logs = rawLogs.stream()
                .map(logMap -> new TransactionInfoResponse.Log("Unknown Event", logMap))
                .toList();

            return TransactionInfoResponse.builder()
                    .hash(hash)
                    .status(status)
                    .blockNumber(blockNumber)
                    .from(from)
                    .to(to)
                    .value(value)
                    .gasUsed(gasUsed)
                    .gasPrice(gasPrice)
                    .timestamp(timestamp)
                    .logs(logs)
                    .build();

        } catch (Exception e) {
            log.error("Failed to get transaction receipt for hash {}", hash, e);
            throw new BlockchainException(
                "Failed to get transaction receipt for hash: " + hash, e);
        }
    }

    public ContractInfoResponse getContractInfo() {
        try {
            Map<String, Object> contractData = besuClient.getContractInfo();

            // Map KRWT info
            Map<String, Object> krwtMap = (Map<String, Object>) contractData.get("krwt");
            ContractInfoResponse.KrwtInfo krwtInfo = ContractInfoResponse.KrwtInfo.builder()
                    .address((String) krwtMap.get("address"))
                    .name((String) krwtMap.get("name"))
                    .symbol((String) krwtMap.get("symbol"))
                    .decimals((Integer) krwtMap.get("decimals"))
                    .totalSupply((String) krwtMap.get("totalSupply"))
                    .owner((String) krwtMap.get("owner"))
                    .build();

            // Map TokenFactory info
            Map<String, Object> tokenFactoryMap = (Map<String, Object>) contractData.get("tokenFactory");
            ContractInfoResponse.TokenFactoryInfo tokenFactoryInfo = ContractInfoResponse.TokenFactoryInfo.builder()
                    .address((String) tokenFactoryMap.get("address"))
                    .tokensCreated((Integer) tokenFactoryMap.get("tokensCreated"))
                    .owner((String) tokenFactoryMap.get("owner"))
                    .build();

            // Map Tokens list
            List<Map<String, Object>> tokensListMap = (List<Map<String, Object>>) contractData.get("tokens");
            List<ContractInfoResponse.TokenDetails> tokenDetailsList = tokensListMap.stream()
                    .map(tokenMap -> ContractInfoResponse.TokenDetails.builder()
                            .projectId((String) tokenMap.get("projectId"))
                            .address((String) tokenMap.get("address"))
                            .dividendAddress((String) tokenMap.get("dividendAddress"))
                            .build())
                    .toList();

            return ContractInfoResponse.builder()
                    .krwt(krwtInfo)
                    .tokenFactory(tokenFactoryInfo)
                    .tokens(tokenDetailsList)
                    .build();

        } catch (Exception e) {
            log.error("Failed to get contract information", e);
            throw new BlockchainException("Failed to get contract information", e);
        }
    }

    @Transactional
    public void settleTradeOnChain(OrderBook buyOrder, OrderBook sellOrder, long qty, long price) {
        String tokenContractAddress = buyOrder.getProduct().getTokenContractAddress();

        // CodeRabbit Feedback #5: tokenContractAddress null 체크 추가
        if (tokenContractAddress == null || tokenContractAddress.isEmpty()) {
            log.warn("SettleTradeOnChain: Skipping settlement due to null or empty tokenContractAddress for product {}.", buyOrder.getProduct().getProductId());
            return;
        }

        User buyer = buyOrder.getVirtualAccount().getUser();
        User seller = sellOrder.getVirtualAccount().getUser();
        String buyerWalletAddress = buyOrder.getVirtualAccount().getWalletAddress();
        String sellerWalletAddress = sellOrder.getVirtualAccount().getWalletAddress();
        long totalKrwtAmount = qty * price;

        try {
            // This is a simplified model where the admin/system wallet facilitates all transfers.
            // A more decentralized model would use approve/transferFrom.

            // 1. Transfer Product Token (Admin -> Buyer)
            // It's assumed the seller's tokens are held in an escrow or by the admin wallet for trading.
            String tokenTxHash = besuClient.transferToken(tokenContractAddress, buyerWalletAddress, qty);
            log.info("Settlement: Transferred {} tokens to buyer {} [Tx: {}]", qty, buyer.getUserId(), tokenTxHash);

            // Log token transfer
            BlockchainTransaction tokenTransaction = BlockchainTransaction.builder()
                    .txHash(tokenTxHash)
                    .fromAddress(besuClient.getAdminAddress()) // From Admin/Escrow
                    .toAddress(buyerWalletAddress)
                    .tokenAddress(tokenContractAddress)
                    .amount(BigDecimal.valueOf(qty))
                    .transactionType(TransactionType.TRADE)
                    .transactionStatus(TransactionStatus.SUCCESS)
                    .user(buyer) // Associated with the buyer
                    .build();
            blockchainTransactionRepository.save(tokenTransaction);

            // 2. Transfer KRWT (Buyer -> Seller)
            String krwtTxHash = besuClient.transferKrwt(sellerWalletAddress, totalKrwtAmount);
            log.info("Settlement: Transferred {} KRWT from buyer {} to seller {} [Tx: {}]", totalKrwtAmount, buyer.getUserId(), seller.getUserId(), krwtTxHash);

            // Log KRWT transfer
            BlockchainTransaction krwtTransaction = BlockchainTransaction.builder()
                    .txHash(krwtTxHash)
                    .fromAddress(besuClient.getAdminAddress()) // CodeRabbit Feedback #6: Changed from buyerWalletAddress to Admin
                    .toAddress(sellerWalletAddress)
                    .tokenAddress(besuClient.getKrwtContractAddress()) // KRWT contract
                    .amount(BigDecimal.valueOf(totalKrwtAmount))
                    .transactionType(TransactionType.TRADE)
                    .transactionStatus(TransactionStatus.SUCCESS)
                    .user(buyer) // Associated with the buyer who initiated the trade
                    .build();
            blockchainTransactionRepository.save(krwtTransaction);

        } catch (Exception e) {
            log.error("On-chain settlement failed for buyOrder {} and sellOrder {}: {}", buyOrder.getOrderId(), sellOrder.getOrderId(), e.getMessage(), e);
            // Re-throw to ensure the entire off-chain transaction is rolled back
            throw new BlockchainException("On-chain settlement failed", e);
        }
    }
}
