package com.masterpiece.IPiece.admin.dividend.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDividendResponse {

    @JsonProperty("dividend_id")
    private Long dividendId;

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("record_date")
    private OffsetDateTime recordDate;

    @JsonProperty("payout_date")
    private OffsetDateTime payoutDate;

    @JsonProperty("total_amount")
    private Long totalAmount;

}