package com.masterpiece.IPiece.common.domain.account;

import com.masterpiece.IPiece.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Entity
@Table(name = "virtual_account_journal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class VirtualAccountJournal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "journal_id")
    private Long journalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private VirtualAccount virtualAccount;

    @Column(name = "tx_type", nullable = false, length = 20)
    private String txType;    // 'DEPOSIT(입금)', 'WITHDRAW(출금)', 'TRADE_BUY', 'TRADE_SELL', 'DIVIDEND'

    @Column(name = "amount_krw", nullable = false)
    private Long amountKrw;   // 입금: +, 출금: -, 매수: -, 매도: +, 배당: +

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter; // 이 이벤트 이후 계좌 잔액

    @Column(name = "number_of_token", nullable = false)
    private Long numberOfToken;

    @Column(name = "description", length = 255)
    private String description;
}