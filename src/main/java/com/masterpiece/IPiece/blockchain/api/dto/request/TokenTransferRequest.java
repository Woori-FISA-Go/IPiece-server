package com.masterpiece.IPiece.blockchain.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenTransferRequest {

    @NotBlank(message = "받는 주소는 필수입니다.")
    @Pattern(regexp = "^0x[0-9a-fA-F]{40}$", message = "올바른 이더리움 주소 형식이 아닙니다.")
    private String toAddress; // 명세의 'to'

    @NotNull(message = "전송 수량은 필수입니다.")
    @Min(value = 1, message = "전송 수량은 1 이상이어야 합니다.")
    private Integer amount;

    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", message = "올바른 UUID 형식이 아닙니다.")
    private String investmentId; // 명세의 'investment_id', Nullable
}
