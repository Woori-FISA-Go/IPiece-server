package com.masterpiece.IPiece.mypage.domain;



import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import com.masterpiece.IPiece.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "holdings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "product_id"}),
        indexes = @Index(name = "idx_holdings_account_product", columnList = "account_id, product_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Holdings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long holdingId;

    @Version
    @Column(name = "version")
    private Long version;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, referencedColumnName = "account_id")
    private VirtualAccount virtualAccount;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Long quantity = 0L;

    @Column(name = "pending_quantity", nullable = false)
    @Builder.Default
    private Long pendingQuantity = 0L;

    @Column(name = "avg_price", nullable = false)
    private Long avgBuyPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false)
    private Product product;

    public void moveToPending(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("이동 수량은 0보다 커야 합니다.");
        }
        if (this.quantity < amount) {
            throw new IllegalStateException("보유 수량이 부족합니다.");
        }
        this.quantity -= amount;
        this.pendingQuantity += amount;
    }

}
