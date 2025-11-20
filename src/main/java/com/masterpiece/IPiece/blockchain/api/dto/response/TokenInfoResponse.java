package com.masterpiece.IPiece.blockchain.api.dto.response;

import com.masterpiece.IPiece.blockchain.domain.BlockchainToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenInfoResponse {
    private String name;
    private String symbol;
    private Long totalSupply;
    private Long faceValue;
    private Long ownerUserId;
    private String contractAddress;

    public static TokenInfoResponse from(BlockchainToken token) {
        return TokenInfoResponse.builder()
                .name(token.getName())
                .symbol(token.getSymbol())
                .totalSupply(token.getTotalSupply())
                .faceValue(token.getFaceValue())
                .ownerUserId(token.getOwnerUserId())
                .contractAddress(token.getContractAddress())
                .build();
    }
}
