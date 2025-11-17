package com.masterpiece.IPiece.blockchain.api.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DividendExecuteRequest {
    private Long projectId;
    private Long totalAmount;
    private String recordDate;
    private String paymentDate;
}
