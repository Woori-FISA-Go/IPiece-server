package com.masterpiece.IPiece.market.api.dto.response;

import lombok.*;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProductDetailsResponse {
    private Info info;
    private Details details;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Info {
        private Long product_id;
        private String product_name;
        private String token_unit;
        private Long current_price;
        private Double change_rate;
        private String thumbnail_img;
        private Boolean is_favorited;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Details {
        private String owner;
        private Long total_supply;
        private String token_standard;
        private String exchange_listing;
        private String description;
        private String detail_image;
        private String detail_url;
        private List<DividendItem> dividends;
        private List<NoticeItem> notices;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DividendItem {
        private Long dividend_id;
        private Long amount_per_token;
        private String payment_date;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class NoticeItem {
        private String disclosure_date;
        private String disclosure_title;
        private String disclosure_url;
    }
}
