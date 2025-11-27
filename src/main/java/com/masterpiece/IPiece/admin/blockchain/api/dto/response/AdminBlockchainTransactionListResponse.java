package com.masterpiece.IPiece.admin.blockchain.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
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

        @JsonProperty("tx_id")
        private Long txId;

        @JsonProperty("tx_hash")
        private String txHash;

        @JsonProperty("status")
        private String status;

        @JsonProperty("tx_type")
        private String txType;

        @JsonProperty("user_id")
        private Long userId;

        @JsonProperty("from_address")
        private String fromAddress;

        @JsonProperty("to_address")
        private String toAddress;

        @JsonProperty("token_address")
        private String tokenAddress;

        @JsonProperty("amount")
        private java.math.BigDecimal amount;

        @JsonProperty("block_number")
        private Long blockNumber;

        @JsonProperty("block_hash")
        private String blockHash;

        @JsonProperty("gas_used")
        private Long gasUsed;

        @JsonProperty("created_at")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private OffsetDateTime createdAt;
    }
}
