package com.masterpiece.IPiece.market.api.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponse {

    private int status_code;
    private String order_id;
    private Long product_id;
    private String side;
    private Long order_price;
    private Long order_quantity;
    private Long total_amount;
    private Long filled_quantity;
    private Long remaining_quantity;
    private String created_at;
    private String idempotency_key;
}
