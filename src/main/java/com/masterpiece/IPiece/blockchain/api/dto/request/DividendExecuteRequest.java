package com.masterpiece.IPiece.blockchain.api.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class DividendExecuteRequest {
    @NotNull(message = "프로젝트 ID는 필수입니다")
    private Long projectId;

    @NotNull(message = "배당 총액은 필수입니다")
    @Positive(message = "배당 총액은 양수여야 합니다")
    private Long totalAmount;

    @NotNull(message = "배당 기준일은 필수입니다")
    private OffsetDateTime recordDate;

    @NotNull(message = "배당 지급일은 필수입니다")
    @FutureOrPresent(message = "배당 지급일은 현재 또는 미래여야 합니다")
    private OffsetDateTime paymentDate;
}
