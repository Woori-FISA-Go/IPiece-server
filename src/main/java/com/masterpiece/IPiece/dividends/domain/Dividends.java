package com.masterpiece.IPiece.dividends.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dividends")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Dividends {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dividend_id")
    private Long dividendId;

    /*
    *******************상품 연결*********************
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    * */

    // 배당 기준일 (언제 보유자 확인할지)
    @Column(name = "record_date", nullable = false)
    private LocalDateTime recordDate;

    // 실제 지급일
    @Column(name = "payout_date", nullable = false)
    private LocalDateTime payoutDate;

    // 전체 배당 금액
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @OneToMany(mappedBy = "dividends", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DividendPayouts> payouts = new ArrayList<>();

    // 양방향 편의 메서드
    public void addPayout(DividendPayouts payout) {
        payouts.add(payout);
        payout.setDividends(this);
    }
}
