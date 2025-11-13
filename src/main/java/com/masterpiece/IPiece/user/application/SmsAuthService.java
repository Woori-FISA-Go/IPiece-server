package com.masterpiece.IPiece.user.application;

import com.masterpiece.IPiece.common.exception.BusinessException;
import com.masterpiece.IPiece.common.exception.ErrorCode;

import com.solapi.sdk.NurigoApp;
import com.solapi.sdk.message.dto.response.MultipleDetailMessageSentResponse;
import com.solapi.sdk.message.dto.response.MultipleMessageSentResponse;
import com.solapi.sdk.message.dto.response.SingleMessageSentResponse;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsAuthService {

    private final Map<String, VerificationData> store = new ConcurrentHashMap<>(); // 폰번호 - 인증번호 매칭
    private final Map<String, VerifiedUserData> verifiedUsers = new ConcurrentHashMap<>(); // 인증 완료 사용자
    private final DefaultMessageService messageService;

    @Value("${solapi.sender}")
    private String senderNumber; // 발신번호

    public SmsAuthService(
            @Value("${solapi.api-key}") String apiKey,
            @Value("${solapi.api-secret}") String apiSecret
    ) {
        // ✅ SDK 1.0.3에서는 NurigoApp을 통해 messageService를 초기화해야 함
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.solapi.com");
    }

    // 내부 클래스: 인증코드 데이터
    private static class VerificationData {
        String code;
        LocalDateTime createdAt;
        VerificationData(String code, LocalDateTime createdAt) {
            this.code = code;
            this.createdAt = createdAt;
        }
    }

    // 내부 클래스: 인증 완료 사용자 데이터
    private static class VerifiedUserData {
        String phone;
        String birth;
        VerifiedUserData(String phone, String birth) {
            this.phone = phone;
            this.birth = birth;
        }
    }

    // ✅ 인증번호 발송
    public void sendVerificationCode(String phone) {
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        store.put(phone, new VerificationData(code, LocalDateTime.now()));


            Message message = new Message();
            message.setFrom(senderNumber);
            message.setTo(phone);
            message.setText("[IPiece] 인증번호는 " + code + " 입니다. (3분 이내 입력)");
        try {
            messageService.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Solapi send failed: " + e.getMessage());
            throw new BusinessException(ErrorCode.SMS_SEND_FAILED);
        }
    }

    // ✅ 인증번호 검증
    public void verifyCode(String phone, String code, String birth) {
        VerificationData data = store.get(phone);
        if (data == null) throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);

        if (data.createdAt.isBefore(LocalDateTime.now().minusMinutes(3))) {
            store.remove(phone);
            throw new BusinessException(ErrorCode.EXPIRED_VERIFICATION_CODE);
        }

        if (!data.code.equals(code)) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        verifiedUsers.put(phone, new VerifiedUserData(phone, birth));
        store.remove(phone);
    }

    public VerifiedUserData getVerifiedUser(String phone) {
        return verifiedUsers.get(phone);
    }

    public void removeVerifiedUser(String phone) {
        verifiedUsers.remove(phone);
    }
}
