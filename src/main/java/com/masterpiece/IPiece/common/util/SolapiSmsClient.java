/*
package com.masterpiece.IPiece.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SolapiSmsClient {

    @Value("${solapi.api-key}")
    private String apiKey;

    @Value("${solapi.api-secret}")
    private String apiSecret;

    @Value("${solapi.sender}")
    private String sender;

    private final ObjectMapper objectMapper = new ObjectMapper();

    */
/**
     * SMS 전송 API 호출
     *//*

    public void sendSms(String to, String text) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "message", Map.of(
                            "to", to,
                            "from", sender,
                            "text", text
                    )
            );

            String requestJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.solapi.com/messages/v4/send"))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "HMAC-SHA256 apiKey=" + apiKey + ", date=" + getDate() + ", salt=" + getSalt() + ", signature=" + getSignature())
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            throw new RuntimeException("SMS 발송 실패", e);
        }
    }

    */
/** Solapi 인증 헤더 생성용 함수들 (공식문서 기준) *//*

    private String getDate() {
        return String.valueOf(System.currentTimeMillis());
    }

    private String getSalt() {
        return java.util.UUID.randomUUID().toString();
    }

    private String getSignature() throws NoSuchAlgorithmException {
        String data = apiKey + getDate() + getSalt();
        return java.util.Base64.getEncoder().encodeToString(
                javax.crypto.Mac.getInstance("HmacSHA256")
                        .doFinal(data.getBytes())
        );
    }
}
*/
