package com.masterpiece.IPiece.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/healthz")
    public ResponseEntity<String> healthCheck() {
        // 쿠버네티스는 200 OK 응답이 오면 정상으로 간주합니다.
        return ResponseEntity.ok("ok");
    }
}