package com.masterpiece.IPiece.user.api;

import com.masterpiece.IPiece.common.web.Responses;
import com.masterpiece.IPiece.user.application.SmsAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/otp")
@RequiredArgsConstructor
public class SmsAuthController {

    private final SmsAuthService smsAuthService;

    /** 인증번호 발송 */
    @PostMapping("/start")
    public ResponseEntity<?> sendCode(@RequestParam String phone) {
        smsAuthService.sendVerificationCode(phone);
        return Responses.ok(Map.of(
                "message", "인증번호가 발송되었습니다."
        ));
    }

    /** 인증번호 검증 */
    @PostMapping("/verify")
    public ResponseEntity<?> verify(
            @RequestParam String phone,
            @RequestParam String code,
            @RequestParam String birth
    ) {
        smsAuthService.verifyCode(phone, code, birth);
        return Responses.ok(Map.of(
                "message", "인증번호 검증 완료",
                "phone", phone,
                "birth", birth,
                "verified", true
        ));
    }
}
