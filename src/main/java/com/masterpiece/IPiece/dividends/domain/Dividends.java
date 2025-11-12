package com.masterpiece.IPiece.dividends.domain;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dividends")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Dividends extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dividend_id")
    private Long dividendId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", unique = true)
    private Product product;

    // 배당 기준일 (언제 보유자 확인할지)
    @Column(name = "record_date", nullable = false)
    private LocalDateTime recordDate;

    // 실제 지급일
    @Column(name = "payout_date", nullable = false)
    private LocalDateTime payoutDate;

    // 전체 배당 금액
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;
    

}
