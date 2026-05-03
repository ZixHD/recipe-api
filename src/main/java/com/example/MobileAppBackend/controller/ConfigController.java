package com.example.MobileAppBackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/config")
public class ConfigController {

    @Value("${APP_VERSION")
    private String appVersion;

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(
                Map.of("status", "UP")
        );
    }

    @GetMapping("/version")
    public ResponseEntity<String> versionCheck() {
        return ResponseEntity.ok("Version: " + appVersion);
    }
}