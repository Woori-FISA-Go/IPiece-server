package com.masterpiece.IPiece.blockchain.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KrwtMintResponse {
    private Long transactionId;
    private Long userId;
    private Long previousBalance;
    private Long mintAmount;
    private Long newBalance;
    private String transactionHash;
    private OffsetDateTime completedAt;
}
