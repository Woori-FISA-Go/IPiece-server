package com.masterpiece.IPiece.admin.blockchain.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBlockchainTransactionListResponse {

    @JsonProperty("items")
    private List<Item> items;

    @JsonProperty("page")
    private Integer page;

    @JsonProperty("page_size")
    private Integer pageSize;

    @JsonProperty("total_count")
    private Long totalCount;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        @JsonProperty("transaction_id")
        private Long transactionId;

        @JsonProperty("tx_hash")
        private String txHash;

        @JsonProperty("transaction_status")
        private String transactionStatus;

        @JsonProperty("transaction_type")
        private String transactionType;

        @JsonProperty("user_id")
        private Long userId;

        @JsonProperty("from_address")
        private String fromAddress;

        @JsonProperty("to_address")
        private String toAddress;

        @JsonProperty("contract_address")
        private String contractAddress;

        @JsonProperty("amount")
        private java.math.BigDecimal amount;

        @JsonProperty("block_number")
        private Long blockNumber;

        @JsonProperty("created_at")
        private OffsetDateTime createdAt;
    }
}