package com.masterpiece.IPiece.user.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
/*
기본생성자 필수 : Spring은 JSON에서 객체로 변환할 때 "기본생성자 + setter" 방식 사용
*/
@AllArgsConstructor
public class SignUpRequest {
    private String id;
    private String password;
    private String name;
    private String address;
    private String phone;      // 본인인증 단계에서 받은 값
    private String birth;      // 본인인증 단계에서 받은 값
    private boolean verified;  // 인증 성공 여부
}
