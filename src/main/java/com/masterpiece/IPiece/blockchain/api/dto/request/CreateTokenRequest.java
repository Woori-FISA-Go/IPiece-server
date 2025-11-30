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
public class CreateTokenRequest {

    @NotBlank(message = "토큰 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "토큰 심볼은 필수입니다.")
    @Pattern(regexp = "^[A-Z]{2,10}$", message = "토큰 심볼은 2-10자의 영문자로만 구성되어야 합니다.")
    private String symbol;

    @NotNull(message = "초기발행수량은 필수입니다.")
    @Min(value = 1, message = "초기발행수량은 1 이상이어야 합니다.")
    private Long totalSupply;

    @NotNull(message = "액면가는 필수입니다.")
    @Min(value = 1, message = "액면가는 1 이상이어야 합니다.")
    private Long faceValue;
}
