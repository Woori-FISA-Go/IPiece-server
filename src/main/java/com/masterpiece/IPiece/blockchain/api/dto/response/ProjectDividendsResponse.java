package com.masterpiece.IPiece.blockchain.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDividendsResponse {
    private ProjectInfo project;
    private List<DividendInfo> dividends;
    private Summary summary;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfo {
        private Long id;
        private String name;
        private String tokenAddress;
        private String dividendAddress;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DividendInfo {
        private Long id;
        private Long totalAmount;
        private Long distributedAmount;
        private Long remainderAmount;
        private Integer recipientCount;
        private Long perShare;
        private String transactionHash;
        private LocalDateTime executedAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Integer totalDistributions;
        private Long totalDistributed;
        private LocalDateTime lastDistribution;
    }
}
