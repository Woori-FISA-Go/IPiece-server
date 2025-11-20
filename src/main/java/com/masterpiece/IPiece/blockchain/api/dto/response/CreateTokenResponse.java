package com.masterpiece.IPiece.blockchain.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTokenResponse {
    private String contractAddress;
    private String transactionHash;
    private String name;
    private String symbol;
    private Long totalSupply;
    private Long faceValue;
    private OffsetDateTime createdAt;
}
