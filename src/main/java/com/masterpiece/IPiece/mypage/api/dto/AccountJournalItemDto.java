package com.masterpiece.IPiece.mypage.api.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountJournalItemDto {

    private Long journalId;
    private String txType;        // "DEPOSIT", "WITHDRAW", "TRADE_BUY", "TRADE_SELL", "DIVIDEND"
    private String description;   // "입금 완료", "출금 완료", "배당금 지급" 등
    private Long amountKrw;       // + / - 포함
    private Long balanceAfter;    // 이 이벤트 이후 잔액
    private LocalDateTime createdAt;
}