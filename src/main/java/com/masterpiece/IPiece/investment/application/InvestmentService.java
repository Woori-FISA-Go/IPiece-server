package com.masterpiece.IPiece.investment.application;

import com.masterpiece.IPiece.blockchain.application.BlockchainService;
import com.masterpiece.IPiece.blockchain.infra.jpa.BlockchainTransactionRepository;
import com.masterpiece.IPiece.blockchain.infra.jpa.WalletRepository;
import com.masterpiece.IPiece.common.domain.product.Product;
import com.masterpiece.IPiece.common.domain.infra.ProductRepository;
import com.masterpiece.IPiece.common.exception.BlockchainException;
import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;
import com.masterpiece.IPiece.common.exception.TokenNotFoundException;
import com.masterpiece.IPiece.common.exception.WalletNotFoundException;
import com.masterpiece.IPiece.investment.api.dto.request.InvestmentRequest;
import com.masterpiece.IPiece.investment.api.dto.response.InvestmentResponse;
import com.masterpiece.IPiece.investment.api.dto.response.InvestmentStatusResponse;
import com.masterpiece.IPiece.blockchain.api.dto.response.TokenTransferResponse;
import com.masterpiece.IPiece.investment.domain.Investment;
import com.masterpiece.IPiece.investment.domain.InvestmentStatus;
import com.masterpiece.IPiece.investment.infra.jpa.InvestmentRepository;
import com.masterpiece.IPiece.user.domain.User;
import com.masterpiece.IPiece.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final BlockchainService blockchainService;
    private final BlockchainTransactionRepository blockchainTransactionRepository; // For transaction history
    private final ProductRepository productRepository; // Inject ProductRepository

    @Transactional
    public InvestmentResponse executeInvestment(Long userId, InvestmentRequest request) {
        // 1. Validate User and Wallet
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String userWalletAddress = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("User " + userId + " has no wallet"))
                .getAddress();

        // 2. Validate Product and get token address
        Product product = productRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        String tokenContractAddress = product.getTokenContractAddress();
        if (tokenContractAddress == null || tokenContractAddress.isEmpty()) {
            throw new BusinessException(ErrorCode.CONTRACT_ADDRESS_NOT_FOUND);
        }

        // 3. Create Investment record with PENDING status
        Investment investment = Investment.builder()
                .user(user)
                .product(product) // Use fetched product
                .amount(BigDecimal.valueOf(request.getAmount()))
                .tokenAmount(request.getTokenAmount().longValue())
                .status(InvestmentStatus.PENDING)
                .build();
        Investment savedInvestment = investmentRepository.save(investment);

        try {
            // 4. Add user's wallet to token's whitelist
            com.masterpiece.IPiece.blockchain.api.dto.request.WhitelistRequest whitelistRequest = new com.masterpiece.IPiece.blockchain.api.dto.request.WhitelistRequest();
            whitelistRequest.setUserWalletAddress(userWalletAddress);
            String whitelistTxHash = blockchainService.addToWhitelist(tokenContractAddress, whitelistRequest);
            savedInvestment.recordWhitelistTx(whitelistTxHash);
            investmentRepository.save(savedInvestment); // Update status to PROCESSING

            // 5. Transfer tokens to user's wallet
            TokenTransferResponse transferResponse = blockchainService.transferToken(tokenContractAddress,
                    com.masterpiece.IPiece.blockchain.api.dto.request.TokenTransferRequest.builder()
                            .toAddress(userWalletAddress)
                            .amount(request.getTokenAmount())
                            .build(),
                    1L); // Assuming admin user ID is 1 for token transfer
            String transferTxHash = transferResponse.getTransactionHash();
            savedInvestment.recordTransferTx(transferTxHash);
            investmentRepository.save(savedInvestment); // Update status to COMPLETED

        } catch (Exception e) {
            log.error("Investment process failed for investment ID: {}", savedInvestment.getId(), e);
            savedInvestment.markFailed();
            investmentRepository.save(savedInvestment);
            throw new BlockchainException("Investment process failed: " + e.getMessage(), e);
        }

        return InvestmentResponse.builder()
                .investmentId(savedInvestment.getId())
                .projectId(savedInvestment.getProduct().getProductId()) // Use getProductId()
                .userWallet(userWalletAddress)
                .tokenAddress(tokenContractAddress)
                .tokenAmount(savedInvestment.getTokenAmount().intValue())
                .krwtSpent(savedInvestment.getAmount().intValue())
                .transactions(InvestmentResponse.Transactions.builder()
                        .whitelist(savedInvestment.getWhitelistTxHash())
                        .transfer(savedInvestment.getTransferTxHash())
                        .build())
                .completedAt(savedInvestment.getUpdatedAt())
                .build();
    }

    public InvestmentStatusResponse getInvestmentStatus(Long userId, Long investmentId) {
        Investment investment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVESTMENT_NOT_FOUND));

        if (!investment.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // For now, dummy token balance. In real scenario, fetch from blockchain.
        InvestmentStatusResponse.TokenBalance tokenBalance = InvestmentStatusResponse.TokenBalance.builder()
                .contractAddress("0xDummyTokenContractAddress")
                .balance(investment.getTokenAmount())
                .confirmed(true)
                .build();

        return InvestmentStatusResponse.of(investment, tokenBalance);
    }
}
