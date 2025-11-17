package com.masterpiece.IPiece.blockchain.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DividendSimulateResponse {
    private boolean canDistribute;
    private String projectName;
    private Long totalSupply;
    private Integer holders;
    private Long amountPerToken;
    private Long estimatedDistributed;
    private Long estimatedRemainder;
    private List<TopHolder> topHolders;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopHolder {
        private String address;
        private Long balance;
        private Long estimatedDividend;
    }
}
