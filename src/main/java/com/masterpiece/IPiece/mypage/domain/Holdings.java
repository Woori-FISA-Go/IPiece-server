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
@Table(name = "holdings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Holdings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long holdingId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, referencedColumnName = "account_id")
    private VirtualAccount virtualAccount;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "avg_price", nullable = false)
    private Long avgBuyPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false)
    private Product product;

}
