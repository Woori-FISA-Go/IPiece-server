package com.masterpiece.IPiece.dividends.domain;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import com.masterpiece.IPiece.common.domain.product.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dividends")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Dividends extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dividend_id")
    private Long dividendId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "record_date", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime recordDate;

    @Column(name = "payout_date", nullable = false, columnDefinition = "timestamptz")
    private OffsetDateTime payoutDate;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'SCHEDULED'") // 기존 데이터에 대한 기본값 설정
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

    /**
     * 배당 배정 완료 시 호출
     * - 분배 금액 / 잔여 금액 / 수령자 수를 기록하고
     *   상태를 PENDING 으로 전환한다.
     */
    public void markAllocated(Long distributedAmount, Long remainderAmount, int recipientCount) {
        this.distributedAmount = distributedAmount;
        this.remainderAmount = remainderAmount;
        this.recipientCount = recipientCount;
        this.status = DividendStatus.PENDING;
    }

    /**
     * 배당 집행이 모두 성공적으로 끝났을 때 호출
     */
    public void markCompleted() {
        this.status = DividendStatus.COMPLETED;
    }

    /**
     * 배당 집행 중 일부/전체 실패가 있을 때 호출
     */
    public void markFailed() {
        this.status = DividendStatus.FAILED;
    }
}

