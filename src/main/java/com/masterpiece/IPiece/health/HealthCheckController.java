package com.masterpiece.IPiece.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping({"/healthz", "/api/healthz"})
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("ok");
    }
}