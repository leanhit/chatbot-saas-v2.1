package com.chatbot.core.tenant.controller;

import com.chatbot.core.tenant.grpc.TenantGrpcClient;
import com.chatbot.core.tenant.grpc.TenantServiceProto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant/grpc-test")
@Slf4j
public class GrpcTestController {

    @Autowired
    private TenantGrpcClient grpcClient;

    @GetMapping("/validate/{tenantKey}")
    public ResponseEntity<Map<String, Object>> validateTenant(@PathVariable String tenantKey) {
        try {
            ValidateTenantResponse response = grpcClient.validateTenant(tenantKey);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response != null);
            if (response != null) {
                result.put("valid", response.getValid());
                result.put("tenantKey", response.getTenantKey());
                result.put("status", response.getStatus());
                result.put("message", response.getMessage());
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi validate tenant qua gRPC", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/exists/{tenantKey}")
    public ResponseEntity<Map<String, Object>> checkTenantExists(@PathVariable String tenantKey) {
        try {
            CheckTenantExistsResponse response = grpcClient.checkTenantExists(tenantKey);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response != null);
            if (response != null) {
                result.put("exists", response.getExists());
                result.put("tenantKey", response.getTenantKey());
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra tenant tồn tại qua gRPC", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/get/{tenantKey}")
    public ResponseEntity<Map<String, Object>> getTenant(@PathVariable String tenantKey) {
        try {
            TenantDetailResponse response = grpcClient.getTenant(tenantKey);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response != null);
            if (response != null) {
                result.put("id", response.getId());
                result.put("tenantKey", response.getTenantKey());
                result.put("name", response.getName());
                result.put("description", response.getDescription());
                result.put("status", response.getStatus());
                result.put("visibility", response.getVisibility());
                result.put("createdAt", response.getCreatedAt());
                result.put("updatedAt", response.getUpdatedAt());
                result.put("expiresAt", response.getExpiresAt());
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi lấy tenant qua gRPC", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testGrpcConnection() {
        try {
            // Test connection bằng cách gọi validateTenant với tenant key không tồn tại
            ValidateTenantResponse response = grpcClient.validateTenant("test-nonexistent-key");
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response != null);
            result.put("message", "gRPC connection test completed");
            if (response != null) {
                result.put("response", Map.of(
                    "valid", response.getValid(),
                    "tenantKey", response.getTenantKey(),
                    "status", response.getStatus(),
                    "message", response.getMessage()
                ));
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi test gRPC connection", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
