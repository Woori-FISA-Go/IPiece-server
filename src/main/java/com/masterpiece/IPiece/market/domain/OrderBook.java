package com.masterpiece.IPiece.market.domain;

import com.masterpiece.IPiece.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_book")
public class OrderBook {

    @Id
    @Column(name = "order_id")
    private Long orderId; // DDL: BIGINT (자동증가 아님)

    // DDL: order_status (BUY/SELL) ← OrderSide로 매핑
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", length = 16, nullable = false)
    private OrderSide side;

    @Column(name = "order_price", nullable = false)
    private Long orderPrice;

    @Column(name = "order_quantity", nullable = false)
    private Long orderQuantity;

    @Column(name = "remain_quantity")
    private Long remainQuantity;

    @Column(name = "createtime", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime createTime;

    // 스키마상 VARCHAR(20)이라 관계 매핑 안 함 — 그대로 보관
    @Column(name = "account_id", length = 20, nullable = false)
    private String accountId;

    // product_id FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id")
    private Product product;

    @Column(name = "create_at", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private OffsetDateTime updatedAt;

    @Column(name = "pending_status")
    private Boolean pendingStatus;
}
