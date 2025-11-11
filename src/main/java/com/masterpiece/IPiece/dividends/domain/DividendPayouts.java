package com.masterpiece.IPiece.dividends.domain;

import com.masterpiece.IPiece.domain.account.VirtualAccount;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    // 양방향 관계 편의 메서드
    public void setDividends(Dividends dividends) {
        this.dividends = dividends;
        if (!dividends.getPayouts().contains(this)) {
            dividends.getPayouts().add(this);
        }
    }
}