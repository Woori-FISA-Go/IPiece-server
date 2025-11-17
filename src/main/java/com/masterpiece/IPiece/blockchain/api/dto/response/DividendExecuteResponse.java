package com.masterpiece.IPiece.blockchain.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DividendExecuteResponse {
    private Long dividendId;
    private Long projectId;
    private String projectName;
    private Long totalAmount;
    private Long distributedAmount;
    private Long remainderAmount;
    private Integer recipientCount;
    private String transactionHash;
    private String status;
    private LocalDateTime executedAt;
}
