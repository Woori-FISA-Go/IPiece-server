package com.masterpiece.IPiece.mypage.api.dto.response;

import com.masterpiece.IPiece.mypage.api.dto.AccountJournalItemDto;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountJournalResponse {

    private Long totalBalance;                  // 현재 보유 현금 (KRW)
    private Long pendingPrice;                 // 거래 대기 금액
    private List<AccountJournalItemDto> items; // 입출금/배당/거래 내역
}