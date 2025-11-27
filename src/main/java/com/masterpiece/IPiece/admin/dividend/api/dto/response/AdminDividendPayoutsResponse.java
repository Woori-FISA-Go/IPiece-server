package com.masterpiece.IPiece.admin.dividend.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDividendPayoutsResponse {

    @JsonProperty("dividend_id")
    private Long dividendId;

    @JsonProperty("recipient_count")
    private Long recipientCount;

    @JsonProperty("total_paid")
    private Long totalPaid;

    @JsonProperty("failed_count")
    private Long failedCount;

    @JsonProperty("items")
    private List<PayoutItem> items;

    @Getter
    @Builder
    public static class PayoutItem {

        @JsonProperty("payout_id")
        private Long payoutId;

        @JsonProperty("account_id")
        private Long accountId;

        @JsonProperty("payout_amount")
        private Long payoutAmount;

        @JsonProperty("payout_status")
        private String payoutStatus; // PAID / FAILED

        @JsonProperty("payout_date")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        private OffsetDateTime payoutDate;
    }
}
