package com.masterpiece.IPiece.blockchain.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WhitelistRequest {

    @NotBlank(message = "사용자의 지갑 주소는 필수입니다.")
    @Pattern(regexp = "^0x[0-9a-fA-F]{40}$", message = "올바른 이더리움 주소 형식이 아닙니다.")
    private String userWalletAddress;
}
