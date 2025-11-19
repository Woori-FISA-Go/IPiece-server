package com.masterpiece.IPiece.market.domain;


import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "trade_execution")
public class TradeExecution extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DDL: BIGSERIAL
    @Column(name = "trade_id")
    private Long tradeId;

    @Column(name = "matchtime", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime matchTime;

    @Column(name = "trade_quantity", nullable = false)
    private Long tradeQuantity;

    @Column(name = "trade_price", nullable = false)
    private Long tradePrice;

    // DDL: BOOLEAN (체결/검증)
    @Column(name = "trade_state", nullable = false)
    private Boolean tradeState;

    @Column(name = "settletime", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime settleTime;

    // N:1 product (FK: product_id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    private Product product;

    // 매수 주문 (N:1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buy_order_id", referencedColumnName = "order_id")
    private OrderBook buyOrder;

    // 매도 주문 (N:1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sell_order_id", referencedColumnName = "order_id")
    private OrderBook sellOrder;

}
