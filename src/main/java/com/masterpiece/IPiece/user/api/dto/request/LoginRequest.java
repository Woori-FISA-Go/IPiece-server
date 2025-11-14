package com.masterpiece.IPiece.user.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String id;  // 로그인 아이디
    private String password;    // 평문 비밀번호
}
