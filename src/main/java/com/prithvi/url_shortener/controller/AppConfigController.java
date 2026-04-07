package com.prithvi.url_shortener.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppConfigController {

    private final String baseUrl;
    private final String displayBaseUrl;

    public AppConfigController(
            @Value("${app.base-url:http://127.0.0.1:8080}") String baseUrl,
            @Value("${app.display-base-url:trim.ly}") String displayBaseUrl) {
        this.baseUrl = baseUrl;
        this.displayBaseUrl = displayBaseUrl;
    }

    @GetMapping(value = "/api/config", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> config() {
        String payload = "{"
                + "\"baseUrl\":\"" + escapeJson(baseUrl) + "\","
                + "\"displayBaseUrl\":\"" + escapeJson(displayBaseUrl) + "\""
                + "}";
        return ResponseEntity.ok(payload);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
