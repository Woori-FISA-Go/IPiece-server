package com.masterpiece.IPiece.main.api;

import com.masterpiece.IPiece.common.exception.ErrorCode;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@Profile("local")
@RestController
@RequestMapping("/v1/_debug")
public class LocalDebugController {

    // 200 OK + 헤더 에코
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping(
            @RequestHeader(value = "Idempotency-Key", required = false) String idem) {

        return ResponseEntity.ok()
                .header("X-Debug", "on")
                .header("X-Idem-Echo", idem != null ? idem : "")
                .body(Map.of(
                        "ok", true,
                        "server_time", Instant.now().toString()
                ));
    }

    // 201 Created
    @PostMapping("/created")
    public ResponseEntity<Map<String, Object>> created() {
        String id = "demo-123";
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", id, "status", "created"));
    }

    // 422 Unprocessable
    @GetMapping("/error")
    public Map<String, Object> error() {
        ErrorCode ec = ErrorCode.UNPROCESSABLE_ENTITY;
        throw new ResponseStatusException(ec.getStatus(), "price는 1 이상이어야 합니다.");
    }
}