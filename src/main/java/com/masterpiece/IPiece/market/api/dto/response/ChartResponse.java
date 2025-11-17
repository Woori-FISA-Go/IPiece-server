package com.masterpiece.IPiece.market.api.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChartResponse {

    private Long product_id;
    private String interval;

    private Window window;

    private List<Point> points;

    private boolean has_more;
    private String next_cursor;
    private String fetched_at;

    @Getter
    @Builder
    public static class Window {
        private String start_at;
        private String end_at;
    }

    @Getter
    @Builder
    public static class Point {
        private String ts;
        private long price;
        private long volume;
    }
}