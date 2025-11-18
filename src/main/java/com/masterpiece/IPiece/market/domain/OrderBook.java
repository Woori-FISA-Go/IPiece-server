package com.masterpiece.IPiece.market.domain;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_book")
public class OrderBook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "version", nullable = false)
    @Builder.Default
    @Version
    private Long version = 0L;

    // DDL: order_status (BUY/SELL) ← OrderSide로 매핑
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 16, nullable = false)
    private OrderType orderType;

    @Column(name = "order_price", nullable = false)
    private Long orderPrice;

    @Column(name = "order_quantity", nullable = false)
    private Long orderQuantity;

    @Column(name = "remain_quantity", nullable = false)
    @Builder.Default
    private Long remainQuantity = 0L;

    @Column(name = "client_time", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime clientTime;

    @Column(name = "pending_status")
    private Boolean pendingStatus;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 64)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", nullable = false)
    private VirtualAccount virtualAccount;

    // product_id FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false)
    private Product product;


}
