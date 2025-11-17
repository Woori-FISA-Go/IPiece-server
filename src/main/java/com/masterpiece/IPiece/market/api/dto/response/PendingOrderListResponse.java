package com.masterpiece.IPiece.market.api.dto.response;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PendingOrderListResponse {

    private List<PendingOrderItem> items;
    private int page;
    private long total;
    private boolean has_next;
}
