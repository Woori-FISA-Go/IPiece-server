package com.masterpiece.IPiece.market.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BuyOrderRequest {

    @NotNull
    @Min(0)
    private Long order_price;

    @NotNull
    @Min(1)
    private Long order_quantity;

    @NotNull
    private String createtime;
}
