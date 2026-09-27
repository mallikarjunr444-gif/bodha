package com.bodha.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${bodha.app.name:BODHA API}")
    private String appName;

    @Value("${bodha.app.version:1.0.0}")
    private String appVersion;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("service", appName);
        response.put("version", appVersion);
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }
}
