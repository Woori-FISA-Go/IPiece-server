package com.masterpiece.IPiece.admin.blockchain.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminBlockchainStatusResponse {

    @JsonProperty("chain_id")
    private String chainId;

    @JsonProperty("network_id")
    private String networkId;

    @JsonProperty("syncing")
    private boolean syncing;

    @JsonProperty("latest_block_number")
    private Long latestBlockNumber;

    @JsonProperty("peer_count")
    private Integer peerCount;

    @JsonProperty("gas_price")
    private Long gasPrice;

    @JsonProperty("latest_block")
    private LatestBlock latestBlock;

    @JsonProperty("healthy")
    private boolean healthy;

    @Getter
    @Builder
    public static class LatestBlock {

        @JsonProperty("number")
        private Long number;

        @JsonProperty("gas_used")
        private Long gasUsed;

        @JsonProperty("gas_limit")
        private Long gasLimit;

        @JsonProperty("tx_count")
        private Integer txCount;
    }
}