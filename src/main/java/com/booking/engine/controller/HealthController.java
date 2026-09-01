package com.booking.engine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Application health and status endpoints")
public class HealthController {

    private final BuildProperties buildProperties;

    @GetMapping
    @Operation(summary = "Get application health", description = "Check if application is running")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("application", "Booking Engine Backend");
        response.put("version", buildProperties.getVersion());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(summary = "Get application info", description = "Get detailed application information")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", buildProperties.getName());
        response.put("version", buildProperties.getVersion());
        response.put("time", buildProperties.getTime());
        response.put("artifact", buildProperties.getArtifact());
        response.put("group", buildProperties.getGroup());
        return ResponseEntity.ok(response);
    }
}
