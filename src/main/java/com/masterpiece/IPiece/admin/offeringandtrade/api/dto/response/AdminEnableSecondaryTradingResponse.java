package com.masterpiece.IPiece.admin.offeringandtrade.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminEnableSecondaryTradingResponse {

    @JsonProperty("success")
    private boolean success;          // 처리 성공 여부

    @JsonProperty("product_id")
    private Long productId;           // 상품 ID

    @JsonProperty("previous_status")
    private String previousStatus;    // 변경 전 상태 (예: "OFFERING")

    @JsonProperty("new_status")
    private String newStatus;         // 변경 후 상태 (예: "TRADE")

    @JsonProperty("enabled_at")
    private OffsetDateTime enabledAt; // 승인/적용 일시 (ISO-8601)

}