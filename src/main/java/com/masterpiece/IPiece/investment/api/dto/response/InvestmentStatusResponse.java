package com.masterpiece.IPiece.investment.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.masterpiece.IPiece.investment.domain.Investment;
import com.masterpiece.IPiece.investment.domain.InvestmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
        private InvestmentStatus status;
        @JsonProperty("transaction_hash")
        private String transactionHash;
        @JsonProperty("completed_at")
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
        if (investment.getWhitelistTxHash() != null) {
            steps.add(Step.builder()
                    .step("whitelist")
                    .status(investment.getStatus() == InvestmentStatus.FAILED ? InvestmentStatus.FAILED : InvestmentStatus.PROCESSING)
                    .transactionHash(investment.getWhitelistTxHash())
                    .completedAt(investment.getUpdatedAt()) // Assuming this is the timestamp for the last update
                    .build());
        }

        // Step 2: Token Transfer
        if (investment.getTransferTxHash() != null) {
            steps.add(Step.builder()
                    .step("token_transfer")
                    .status(InvestmentStatus.COMPLETED)
                    .transactionHash(investment.getTransferTxHash())
                    .completedAt(investment.getUpdatedAt())
                    .build());
        }


        return InvestmentStatusResponse.builder()
                .investmentId(investment.getId())
                .blockchainStatus(investment.getStatus())
                .steps(steps)
                .tokenBalance(tokenBalance)
                .build();
    }
}
