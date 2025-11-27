package com.masterpiece.IPiece.blockchain.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

 @Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class MyWalletResponse {
    
    private String walletAddress;
    private Long balanceKrw;              // balance_krw 사용
    private List<TokenInfo> tokens;
    private Long totalValueKrw;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private OffsetDateTime createdAt;     // OffsetDateTime 사용
    
    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TokenInfo {
        private Long projectId;
        private String projectName;
        private String tokenAddress;
        private String symbol;
        private Long balance;
        private Long totalSupply;
        private String sharePercentage;
        private Long totalDividendsReceived;
    }
}
