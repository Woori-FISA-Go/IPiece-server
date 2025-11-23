package com.masterpiece.IPiece.blockchain.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractInfoResponse {
    private KrwtInfo krwt;
    private TokenFactoryInfo tokenFactory;
    private List<TokenDetails> tokens;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KrwtInfo {
        private String address;
        private String name;
        private String symbol;
        private Integer decimals;
        private String totalSupply;
        private String owner;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TokenFactoryInfo {
        private String address;
        private Integer tokensCreated;
        private String owner;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TokenDetails {
        private String projectId; // UUID 형식의 프로젝트 ID
        private String address; // 토큰 컨트랙트 주소
        private String dividendAddress; // 배당 컨트랙트 주소
    }
}
