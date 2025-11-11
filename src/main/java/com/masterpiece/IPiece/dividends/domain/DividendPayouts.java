package com.masterpiece.IPiece.dividends.domain;

import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dividend_payouts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DividendPayouts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payout_id")
    private Long payoutId;

    // 어떤 배당 계획에서 나온 지급인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dividend_id", nullable = false)
    private Dividends dividends;

    // 어떤 계좌(사용자)에게 지급됐는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private VirtualAccount virtualAccount;

    @Column(name = "payout_amount", nullable = false)
    private Long payoutAmount;

    @Column(name = "payout_status", nullable = false, length = 20)
    private String payoutStatus; // 예: "PENDING", "COMPLETED", "FAILED"

    @Column(name = "payout_date", nullable = false)
    private LocalDateTime payoutDate;

    @CreationTimestamp
    @Column(name = "create_at", columnDefinition = "timestamptz", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz")
    private LocalDateTime updateAt;

}