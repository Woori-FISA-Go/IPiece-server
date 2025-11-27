package com.masterpiece.IPiece.investment.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

@Getter
@Builder
public class InvestmentResponse {

    @JsonProperty("investment_id")
    private Long investmentId;

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("user_wallet")
    private String userWallet;

    @JsonProperty("token_address")
    private String tokenAddress;

    @JsonProperty("token_amount")
    private Integer tokenAmount;

    @JsonProperty("krwt_spent")
    private Integer krwtSpent;

    @JsonProperty("transactions")
    private Transactions transactions;

    @JsonProperty("completed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private OffsetDateTime completedAt;

    @Getter
    @Builder
    public static class Transactions {
        @JsonProperty("whitelist")
        private String whitelist;

        @JsonProperty("transfer")
        private String transfer;
    }
}
