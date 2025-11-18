package com.masterpiece.IPiece.market.infra.jpa.projection;

public interface OrderBookItemProjection {
    Long getPrice();
    Long getQty();
}