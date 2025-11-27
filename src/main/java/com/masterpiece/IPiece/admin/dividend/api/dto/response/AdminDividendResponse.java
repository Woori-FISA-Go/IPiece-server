package com.masterpiece.IPiece.admin.dividend.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private OffsetDateTime recordDate;

    @JsonProperty("payout_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private OffsetDateTime payoutDate;

    @JsonProperty("total_amount")
    private Long totalAmount;

}
