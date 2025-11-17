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
public class MyDividendsResponse {
    private List<DividendInfo> dividends;
    private Summary summary;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DividendInfo {
        private Long dividendId;
        private ProjectInfo project;
        private Long amount;
        private Long myTokenBalance;
        private Long totalSupply;
        private String mySharePercentage;
        private String transactionHash;
        private LocalDateTime paidAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectInfo {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long totalReceived;
        private Integer dividendCount;
        private Integer projectsCount;
    }
}
