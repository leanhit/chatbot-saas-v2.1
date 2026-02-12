package com.chatbot.core.identity.controller;

import com.chatbot.core.identity.service.TenantGrpcService;
import com.chatbot.core.tenant.grpc.TenantServiceProto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/identity-inter-hub")
@Slf4j
public class IdentityInterHubController {

    @Autowired
    private TenantGrpcService tenantGrpcService;

    /**
     * Identity Hub gọi Tenant Hub để validate tenant
     */
    @PostMapping("/identity/validate-tenant")
    public ResponseEntity<Map<String, Object>> validateTenant(@RequestBody Map<String, String> request) {
        try {
            String tenantKey = request.get("tenantKey");
            boolean isValid = tenantGrpcService.validateTenant(tenantKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("tenantKey", tenantKey);
            response.put("message", isValid ? "Tenant hợp lệ" : "Tenant không hợp lệ");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi validate tenant", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Identity Hub gọi Tenant Hub để check tenant exists
     */
    @PostMapping("/identity/check-tenant")
    public ResponseEntity<Map<String, Object>> checkTenantExists(@RequestBody Map<String, String> request) {
        try {
            String tenantKey = request.get("tenantKey");
            boolean exists = tenantGrpcService.checkTenantExists(tenantKey);
            
            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);
            response.put("tenantKey", tenantKey);
            response.put("message", exists ? "Tenant tồn tại" : "Tenant không tồn tại");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi check tenant exists", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Identity Hub gọi Tenant Hub để get tenant
     */
    @GetMapping("/identity/tenant/{tenantKey}")
    public ResponseEntity<TenantDetailResponse> getTenant(@PathVariable String tenantKey) {
        try {
            TenantDetailResponse tenant = tenantGrpcService.getTenant(tenantKey);
            
            if (tenant != null) {
                return ResponseEntity.ok(tenant);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Lỗi khi get tenant", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Identity Hub gọi Tenant Hub để list tenants
     */
    @GetMapping("/identity/tenants")
    public ResponseEntity<ListTenantsResponse> listTenants(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            ListTenantsResponse tenants = tenantGrpcService.listTenants(page, size);
            
            if (tenants != null) {
                return ResponseEntity.ok(tenants);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("Lỗi khi list tenants", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Identity Hub gọi Tenant Hub để create tenant
     */
    @PostMapping("/identity/create-tenant")
    public ResponseEntity<TenantResponse> createTenant(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String description = request.get("description");
            String tenantKey = request.get("tenantKey");
            String visibility = request.get("visibility");
            String ownerEmail = request.get("ownerEmail");
            
            TenantResponse tenant = tenantGrpcService.createTenant(
                    name, description, tenantKey, visibility, ownerEmail);
            
            if (tenant != null) {
                return ResponseEntity.ok(tenant);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("Lỗi khi create tenant", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
