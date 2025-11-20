package com.masterpiece.IPiece.mypage.api.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.masterpiece.IPiece.common.domain.account.VirtualAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccountResponse {

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("account_no")
    private String accountNo;

    @JsonProperty("balance_krw")
    private Long balanceKrw;

    @JsonProperty("pending_price")
    private Long pendingPrice;

    @JsonProperty("wallet_address")
    private String walletAddress;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("created")
    private boolean created;

    public static VirtualAccountResponse from(VirtualAccount v, boolean created) {
        return VirtualAccountResponse.builder()
                .accountId(v.getAccountId())
                .accountNo(v.getAccountNo())
                .balanceKrw(v.getBalanceKrw())
                .pendingPrice(v.getPendingPrice())
                .walletAddress(v.getWalletAddress())
                .userId(v.getUser().getUserId())
                .created(created)
                .build();
    }
}