package com.masterpiece.IPiece.mypage.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.masterpiece.IPiece.mypage.api.dto.AccountHistoryItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

    /**
     * /v1/mypage/account 응답 루트 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class AccountHistoryResponse {

        @JsonProperty("total_balance")
        private Long totalBalance;   // VirtualAccount.balanceKrw

        @JsonProperty("pending_price")
        private Long pendingPrice;   // VirtualAccount.pendingPrice

        @JsonProperty("history")
        private List<AccountHistoryItemDto> history;  // 거래내역 (없으면 빈 리스트)
    }
