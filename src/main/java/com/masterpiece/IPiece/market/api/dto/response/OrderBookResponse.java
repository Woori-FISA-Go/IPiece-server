package com.masterpiece.IPiece.market.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OrderBookResponse {

    private Summary summary;
    private List<OrderBookItem> orders_sell;
    private List<OrderBookItem> orders_buy;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private Long highest_price;
        private Long lowest_price;
        private Long last_price;
        private double price_change;
        private Long limit_up_price;
        private Long limit_down_price;
        private Long this_week_volume;
        private Long last_week_volume;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class OrderBookItem {
        private Long order_price;
        private Long quantity;
        private double price_change;
    }
}

