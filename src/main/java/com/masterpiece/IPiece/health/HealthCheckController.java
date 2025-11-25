package com.masterpiece.IPiece.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping("/healthz")
    public ResponseEntity<String> healthCheck() {
        // 단순히 200 OK와 "ok" 문자열만 반환하면 쿠버네티스는 "살아있다"고 판단합니다.
        return ResponseEntity.ok("ok");
    }
}