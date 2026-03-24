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

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listTenants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("REST: Bắt đầu list tenants - page: {}, size: {}", page, size);
            ListTenantsResponse response = grpcClient.listTenants(page, size);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response != null);
            if (response != null) {
                result.put("tenants", response.getTenantsList());
                result.put("totalElements", response.getTotalElements());
                result.put("totalPages", response.getTotalPages());
                result.put("currentPage", response.getCurrentPage());
                log.info("REST: List tenants thành công, trả về {} tenants", response.getTenantsCount());
            } else {
                log.warn("REST: listTenants response là null");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi liệt kê tenants qua gRPC", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchTenants(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("REST: Bắt đầu search tenants - query: {}, page: {}, size: {}", query, page, size);
            SearchTenantsRequest request = SearchTenantsRequest.newBuilder()
                    .setQuery(query)
                    .setPage(page)
                    .setSize(size)
                    .build();
            
            SearchTenantsResponse response = grpcClient.searchTenants(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response != null);
            if (response != null) {
                result.put("tenants", response.getTenantsList());
                result.put("totalElements", response.getTotalElements());
                result.put("totalPages", response.getTotalPages());
                result.put("currentPage", response.getCurrentPage());
                log.info("REST: Search tenants thành công, trả về {} tenants", response.getTenantsCount());
            } else {
                log.warn("REST: searchTenants response là null");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi search tenants qua gRPC", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/suspend/{tenantKey}")
    public ResponseEntity<Map<String, Object>> suspendTenant(@PathVariable String tenantKey) {
        try {
            SuspendTenantRequest request = SuspendTenantRequest.newBuilder()
                    .setTenantKey(tenantKey)
                    .setReason("Test suspend")
                    .build();
            
            com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse response = 
                grpcClient.suspendTenant(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response != null);
            if (response != null) {
                result.put("id", response.getId());
                result.put("tenantKey", response.getTenantKey());
                result.put("name", response.getName());
                result.put("status", response.getStatus());
                result.put("message", "Tenant suspended successfully");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi suspend tenant qua gRPC", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/activate/{tenantKey}")
    public ResponseEntity<Map<String, Object>> activateTenant(@PathVariable String tenantKey) {
        try {
            ActivateTenantRequest request = ActivateTenantRequest.newBuilder()
                    .setTenantKey(tenantKey)
                    .build();
            
            com.chatbot.core.tenant.grpc.TenantServiceProto.TenantResponse response = 
                grpcClient.activateTenant(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response != null);
            if (response != null) {
                result.put("id", response.getId());
                result.put("tenantKey", response.getTenantKey());
                result.put("name", response.getName());
                result.put("status", response.getStatus());
                result.put("message", "Tenant activated successfully");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi activate tenant qua gRPC", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/status/{tenantKey}")
    public ResponseEntity<Map<String, Object>> getTenantStatus(@PathVariable String tenantKey) {
        try {
            GetTenantStatusRequest request = GetTenantStatusRequest.newBuilder()
                    .setTenantKey(tenantKey)
                    .build();
            
            TenantStatusResponse response = grpcClient.getTenantStatus(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response != null);
            if (response != null) {
                result.put("tenantKey", response.getTenantKey());
                result.put("status", response.getStatus());
                result.put("message", response.getMessage());
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi khi get tenant status qua gRPC", e);
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
