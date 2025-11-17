package com.masterpiece.IPiece.market.api.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PendingOrderItem {

    private String order_id;
    private Long product_id;
    private String product_name;
    private String order_type;
    private Long price;
    private Long quantity;
    private Long filled_quantity;
    private Long remaining_quantity;
    private Long amount;
    private String placed_at;
}