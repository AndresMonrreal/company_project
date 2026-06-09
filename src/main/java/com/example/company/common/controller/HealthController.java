package com.example.company.common.controller;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "message", "Backend is listening",
                "service", "company",
                "timestamp", LocalDateTime.now()
        );
    }
}
