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
public class MyDividendsResponse {
    private Summary summary;
    private List<DividendItem> items;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DividendItem {
        private Long dividendId;
        private ProjectInfo project;
        private Long amount;
        private OffsetDateTime paidAt;
        private String status;
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