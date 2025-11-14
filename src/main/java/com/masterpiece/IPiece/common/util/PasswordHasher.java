package com.masterpiece.IPiece.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

// 비밀번호 해시/검증 유틸(회원가입 시에는 encode(), 로그인 시에는 matches())

@Component
public class PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    //회원가입 시
    public String encode(String password) {
        return encoder.encode(password);
    }

    //로그인 시 평문과 해시 비교
    public boolean matches(String password, String encodedPassword) {
        return encoder.matches(password, encodedPassword);
    }
}
