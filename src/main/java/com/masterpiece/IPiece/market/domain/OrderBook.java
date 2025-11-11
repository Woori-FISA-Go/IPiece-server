package com.masterpiece.IPiece.market.domain;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_book")
public class OrderBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    // DDL: order_status (BUY/SELL) ← OrderSide로 매핑
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 16, nullable = false)
    private OrderType orderType;

    @Column(name = "order_price", nullable = false)
    private Long orderPrice;

    @Column(name = "order_quantity", nullable = false)
    private Long orderQuantity;

    @Column(name = "remain_quantity")
    private Long remainQuantity;

    @Column(name = "createtime", columnDefinition = "timestamptz", nullable = false)
    private LocalDateTime createTime;

    @CreationTimestamp
    @Column(name = "create_at", columnDefinition = "timestamptz", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private LocalDateTime updatedAt;

    @Column(name = "pending_status")
    private Boolean pendingStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", nullable = false)
    private VirtualAccount virtualAccount;

    // product_id FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false)
    private Product product;


}
