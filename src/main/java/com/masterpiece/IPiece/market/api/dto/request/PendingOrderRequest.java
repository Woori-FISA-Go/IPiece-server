package com.masterpiece.IPiece.market.api.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PendingOrderRequest {

    @Min(1)
    private Integer page = 1;
}
