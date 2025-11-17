package com.masterpiece.IPiece.blockchain.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DividendSimulateRequest {
    @NotNull(message = "프로젝트 ID는 필수입니다")
    private Long projectId;

    @NotNull(message = "배당 총액은 필수입니다")
    @Positive(message = "배당 총액은 양수여야 합니다")
    private Long totalAmount;
}
