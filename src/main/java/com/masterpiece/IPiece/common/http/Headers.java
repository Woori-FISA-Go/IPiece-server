package com.masterpiece.IPiece.common.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 공통 헤더 상수/빌더 유틸.
 * - 서버가 외부 서비스 호출할 때(RestClient/WebClient) 재사용
 * - 컨트롤러/필터에서도 상수 재사용
 */
public final class Headers {

    private Headers() {}

    /** 표준 헤더 키 */
    public static final class Names {
        public static final String AUTHORIZATION   = HttpHeaders.AUTHORIZATION;          // "Authorization"
        public static final String CONTENT_TYPE    = HttpHeaders.CONTENT_TYPE;           // "Content-Type"
        public static final String ACCEPT          = HttpHeaders.ACCEPT;                 // "Accept"
        public static final String ACCEPT_LANGUAGE = HttpHeaders.ACCEPT_LANGUAGE;        // "Accept-Language"
        public static final String CONTENT_LANGUAGE= HttpHeaders.CONTENT_LANGUAGE;       // "Content-Language"
        public static final String IDEMPOTENCY_KEY = "Idempotency-Key";
        public static final String X_REQUEST_ID    = "X-Request-Id";
        private Names() {}
    }

    /** 값/포맷 */
    public static final class Values {
        public static final MediaType JSON = MediaType.APPLICATION_JSON;
        public static final String BEARER_PREFIX = "Bearer ";
        private Values() {}
    }

    /* ===========================
       빌더(외부 호출시 재사용)
       =========================== */

    /** 본문 없는 비로그인 요청용 */
    public static HttpHeaders jsonAcceptOnly() {
        HttpHeaders h = new HttpHeaders();
        h.setAccept(java.util.List.of(Values.JSON));
        return h;
    }

    /** 본문 있는 비로그인 요청용 */
    public static HttpHeaders json() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(Values.JSON);
        h.setAccept(java.util.List.of(Values.JSON));
        return h;
    }

    /** 로그인(토큰 포함) 요청용 */
    public static HttpHeaders jsonWithBearer(String jwt) {
        HttpHeaders h = json();
        h.set(Names.AUTHORIZATION, ensureBearer(jwt));
        return h;
    }

    /** 멱등성 필요한 거래/공모 참여 요청용 */
    public static HttpHeaders jsonWithBearerAndIdempotency(String jwt, UUID idempotencyKey) {
        HttpHeaders h = jsonWithBearer(jwt);
        h.set(Names.IDEMPOTENCY_KEY, Objects.requireNonNull(idempotencyKey, "idempotencyKey").toString());
        return h;
    }

    /** Accept-Language/Content-Language 간단 설정 */
    public static void setLanguage(HttpHeaders headers, Locale locale) {
        Optional.ofNullable(headers).ifPresent(h -> {
            if (locale != null) {
                h.set(Names.ACCEPT_LANGUAGE, locale.toLanguageTag());
                h.set(Names.CONTENT_LANGUAGE, locale.toLanguageTag());
            }
        });
    }

    /* ===========================
       파서/검증 헬퍼
       =========================== */

    /** "Bearer xxx" 형태 보장 */
    public static String ensureBearer(String rawToken) {
        String t = Objects.requireNonNull(rawToken, "jwt");
        return t.startsWith(Values.BEARER_PREFIX) ? t : Values.BEARER_PREFIX + t;
    }

    /** Idempotency-Key UUID 여부 검사 (유효하지 않으면 IllegalArgumentException) */
    public static UUID validateIdempotencyKey(String key) {
        try {
            return UUID.fromString(Objects.requireNonNull(key, "Idempotency-Key"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Idempotency-Key (UUID required)");
        }
    }
}