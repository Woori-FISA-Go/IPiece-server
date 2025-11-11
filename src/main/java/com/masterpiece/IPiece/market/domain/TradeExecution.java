package com.masterpiece.IPiece.market.domain;


import com.masterpiece.IPiece.domain.product.Product;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "trade_execution")
public class TradeExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DDL: BIGSERIAL
    @Column(name = "trade_id")
    private Long tradeId;

    @Column(name = "matchtime", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime matchTime;

    @Column(name = "trade_quantity", nullable = false)
    private Long tradeQuantity;

    // DDL: BOOLEAN (체결/검증)
    @Column(name = "trade_state", nullable = false)
    private Boolean tradeState;

    @Column(name = "settletime", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime settleTime;

    // N:1 product (FK: product_id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    private Product product;

    // N:1 buy order (FK: order_book.order_id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buy_order_id", referencedColumnName = "order_id")
    private OrderBook buyOrder;

    // sell_order_id 는 DDL 상 CopyOforder_book 을 참조하므로,
    // 여기서는 ID 값만 그대로 보관(추가 엔티티 생성하지 않음).
    @Column(name = "sell_order_id", nullable = false)
    private Long sellOrderId;

    @Column(name = "trade_price", nullable = false)
    private Long tradePrice;

    @Column(name = "create_at", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;
}
