package com.masterpiece.IPiece.common.web;

import org.springframework.http.*;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 상태코드 규칙에 맞춘 공통 응답 헬퍼.
 * - 성공(200/201/202/204)
 * - 오류(400/401/403/404/409/422/429/500/502/503/504)
 * - RFC7807 problem+json 간단 빌더 포함
 */
public final class Responses {
    private Responses() {}
    public static final MediaType PROBLEM_JSON = MediaType.valueOf("application/problem+json");

    /* -------------------- 성공 응답 -------------------- */

    // 200 OK
    public static <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok(body);
    }

    // 201 Created (+ Location 헤더)
    public static <T> ResponseEntity<T> created(T body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // 202 Accepted (비동기 접수)
    public static <T> ResponseEntity<T> accepted(T body) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }

    // 204 No Content
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /* -------------------- 오류 응답 (problem+json) -------------------- */

    // 400 Bad Request
    public static ResponseEntity<Map<String, Object>> badRequest(String type, String title, String detail, String instance) {
        return problem(HttpStatus.BAD_REQUEST, type, title, detail, instance, null);
    }

    // 401 Unauthorized
    public static ResponseEntity<Map<String, Object>> unauthorized(String type, String title, String detail, String instance) {
        return problem(HttpStatus.UNAUTHORIZED, type, title, detail, instance, null);
    }

    // 403 Forbidden
    public static ResponseEntity<Map<String, Object>> forbidden(String type, String title, String detail, String instance) {
        return problem(HttpStatus.FORBIDDEN, type, title, detail, instance, null);
    }

    // 404 Not Found
    public static ResponseEntity<Map<String, Object>> notFound(String type, String title, String detail, String instance) {
        return problem(HttpStatus.NOT_FOUND, type, title, detail, instance, null);
    }

    // 409 Conflict
    public static ResponseEntity<Map<String, Object>> conflict(String type, String title, String detail, String instance) {
        return problem(HttpStatus.CONFLICT, type, title, detail, instance, null);
    }

    // 422 Unprocessable Entity
    public static ResponseEntity<Map<String, Object>> unprocessable(String type, String title, String detail, String instance) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, type, title, detail, instance, null);
    }

    // 429 Too Many Requests (+ Retry-After)
    public static ResponseEntity<Map<String, Object>> tooManyRequests(String type, String title, String detail, String instance, Duration retryAfter) {
        HttpHeaders h = new HttpHeaders();
        if (retryAfter != null) h.set(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter.toSeconds()));
        return problem(HttpStatus.TOO_MANY_REQUESTS, type, title, detail, instance, h);
    }

    // 500 Internal Server Error
    public static ResponseEntity<Map<String, Object>> internalError(String type, String title, String detail, String instance) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, type, title, detail, instance, null);
    }

    // 502 Bad Gateway
    public static ResponseEntity<Map<String, Object>> badGateway(String type, String title, String detail, String instance) {
        return problem(HttpStatus.BAD_GATEWAY, type, title, detail, instance, null);
    }

    // 503 Service Unavailable (+ Retry-After)
    public static ResponseEntity<Map<String, Object>> unavailable(String type, String title, String detail, String instance, Duration retryAfter) {
        HttpHeaders h = new HttpHeaders();
        if (retryAfter != null) h.set(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter.toSeconds()));
        return problem(HttpStatus.SERVICE_UNAVAILABLE, type, title, detail, instance, h);
    }

    // 504 Gateway Timeout
    public static ResponseEntity<Map<String, Object>> gatewayTimeout(String type, String title, String detail, String instance) {
        return problem(HttpStatus.GATEWAY_TIMEOUT, type, title, detail, instance, null);
    }

    /* -------------------- 공통 problem 빌더 -------------------- */

    public static ResponseEntity<Map<String, Object>> problem(
            HttpStatus status,
            String type, String title, String detail, String instance,
            HttpHeaders extraHeaders
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("type", type);
        body.put("title", title);
        body.put("status", status.value());
        body.put("detail", detail);
        body.put("instance", instance);

        HttpHeaders headers = (extraHeaders != null) ? extraHeaders : new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);

        return new ResponseEntity<>(body, headers, status);
    }
}