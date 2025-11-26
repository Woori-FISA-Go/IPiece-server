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
public class TokenTransferResponse {
    private String fromAddress; // 명세의 'from'
    private String toAddress;   // 명세의 'to'
    private Long amount;
    private String transactionHash;
    private OffsetDateTime transferredAt; // 명세의 'transferred_at'
}
