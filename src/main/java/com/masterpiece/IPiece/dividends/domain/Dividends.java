package com.masterpiece.IPiece.dividends.domain;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "record_date", nullable = false)
    private OffsetDateTime recordDate;

    @Column(name = "payout_date", nullable = false)
    private OffsetDateTime payoutDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private DividendStatus status = DividendStatus.SCHEDULED;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Column(name = "distributed_amount")
    private Long distributedAmount;

    @Column(name = "remainder_amount")
    private Long remainderAmount;

    @Column(name = "recipient_count")
    private Integer recipientCount;

    @Column(name = "transaction_hash", length = 66)
    private String transactionHash;

    @Column(name = "block_number")
    private Long blockNumber;

    @OneToMany(mappedBy = "dividends", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DividendPayouts> dividendPayouts;
}
