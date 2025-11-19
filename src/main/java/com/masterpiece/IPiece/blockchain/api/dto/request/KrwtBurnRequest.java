package com.masterpiece.IPiece.blockchain.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KrwtBurnRequest {
    @NotNull(message = "출금할 금액은 필수입니다.")
    @Min(value = 1, message = "출금 금액은 0보다 커야 합니다.")
    private Long amount;

    private String memo;
}
