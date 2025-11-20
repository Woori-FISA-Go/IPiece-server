package com.masterpiece.IPiece.admin.offeringandtrade.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminEnableSecondaryTradingRequest {

    @NotNull(message = "confirm은 필수 값입니다.")
    @JsonProperty("confirm")
    private Boolean confirm;   // 승인 여부(항상 true 기대)
}