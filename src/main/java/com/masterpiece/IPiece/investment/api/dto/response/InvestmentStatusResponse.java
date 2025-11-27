package com.masterpiece.IPiece.investment.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.masterpiece.IPiece.investment.domain.Investment;
import com.masterpiece.IPiece.investment.domain.InvestmentStatus;
import com.masterpiece.IPiece.investment.domain.InvestmentStepStatus;
import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
public class InvestmentStatusResponse {

    @JsonProperty("investment_id")
    private Long investmentId;

    @JsonProperty("blockchain_status")
    private InvestmentStatus blockchainStatus;

    @JsonProperty("steps")
    private List<Step> steps;

    @JsonProperty("token_balance")
    private TokenBalance tokenBalance;

    @Getter
    @Builder
    public static class Step {
        private String step;
        private InvestmentStepStatus status; // Changed to InvestmentStepStatus
        @JsonProperty("transaction_hash")
        private String transactionHash;
        @JsonProperty("completed_at")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private OffsetDateTime completedAt;
    }

    @Getter
    @Builder
    public static class TokenBalance {
        @JsonProperty("contract_address")
        private String contractAddress;
        private long balance;
        private boolean confirmed;
    }

    public static InvestmentStatusResponse of(Investment investment, TokenBalance tokenBalance) {
        List<Step> steps = new ArrayList<>();

        // Step 1: Whitelist
        Optional.ofNullable(investment.getWhitelistTxHash()).ifPresent(txHash ->
            steps.add(Step.builder()
                    .step("whitelist")
                    .status(investment.getWhitelistStatus())
                    .transactionHash(txHash)
                    .completedAt(investment.getWhitelistCompletedAt())
                    .build())
        );

        // Step 2: Token Transfer
        Optional.ofNullable(investment.getTransferTxHash()).ifPresent(txHash ->
            steps.add(Step.builder()
                    .step("token_transfer")
                    .status(investment.getTransferStatus())
                    .transactionHash(txHash)
                    .completedAt(investment.getTransferCompletedAt())
                    .build())
        );

        return InvestmentStatusResponse.builder()
                .investmentId(investment.getId())
                .blockchainStatus(investment.getStatus())
                .steps(steps)
                .tokenBalance(tokenBalance)
                .build();
    }
}
