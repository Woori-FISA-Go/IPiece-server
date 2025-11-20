package com.masterpiece.IPiece.admin.dividend.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminUpsertDividendRequest {

    @JsonProperty("dividend_id")
    private Long dividendId;   // 없으면 생성, 있으면 수정

    @NotNull(message = "product_id는 필수입니다.")
    @JsonProperty("product_id")
    private Long productId;

    @NotNull(message = "record_date는 필수입니다.")
    @JsonProperty("record_date")
    private String recordDate; // "2026-03-15T23:59:59+09:00"

    @NotNull(message = "payout_date는 필수입니다.")
    @JsonProperty("payout_date")
    private String payoutDate; // "2026-03-31T10:00:00+09:00"

    @NotNull(message = "total_amount는 필수입니다.")
    @Min(value = 1, message = "total_amount는 1 이상이어야 합니다.")
    @JsonProperty("total_amount")
    private Long totalAmount;
}