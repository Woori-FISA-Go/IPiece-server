package com.masterpiece.IPiece.blockchain.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDividendsResponse {
    private Long projectId;
    private String projectName;
    private Long totalDistributedAmount;
    private Long totalRemainderAmount;
    private Integer totalRecipientCount;
    private List<DividendItem> items;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DividendItem {
        private Long dividendId;
        private Long totalAmount;
        private Long distributedAmount;
        private Long remainderAmount;
        private Integer recipientCount;
        private String transactionHash;
        private String status;
        private OffsetDateTime executedAt;
        private OffsetDateTime lastDistribution;
    }
}