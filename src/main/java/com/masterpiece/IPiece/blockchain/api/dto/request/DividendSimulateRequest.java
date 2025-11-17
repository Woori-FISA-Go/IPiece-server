package com.masterpiece.IPiece.blockchain.api.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DividendSimulateRequest {
    private Long projectId;
    private Long totalAmount;
}
