package com.masterpiece.IPiece.blockchain.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WhitelistRequest {

    @NotBlank(message = "사용자의 지갑 주소는 필수입니다.")
    private String userWalletAddress;
}
