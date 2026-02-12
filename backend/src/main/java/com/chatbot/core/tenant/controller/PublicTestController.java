package com.chatbot.core.tenant.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@Slf4j
public class PublicTestController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "tenant-service");
        response.put("grpc", "running on port 50052");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/grpc-status")
    public ResponseEntity<Map<String, Object>> grpcStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("grpc_server", "running");
        response.put("port", 50052);
        response.put("status", "active");
        response.put("message", "gRPC Tenant Service is running");
        return ResponseEntity.ok(response);
    }
}
