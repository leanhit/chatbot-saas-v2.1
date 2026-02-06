package com.chatbot.modules.test.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * User and Tenant Address Test Controller
 */
@RestController
@RequestMapping("/api/test/address")
public class UserTenantAddressController {
    
    /**
     * Get current user address information
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserAddress(Principal principal) {
        Map<String, Object> userInfo = Map.of(
            "userId", principal != null ? principal.getName() : "anonymous",
            "userType", principal != null ? principal.getClass().getSimpleName() : "none",
            "isAuthenticated", principal != null,
            "address", Map.of(
                "type", "USER_ADDRESS",
                "street", "123 User Street",
                "city", "User City",
                "country", "Vietnam",
                "postalCode", "100000"
            ),
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(userInfo);
    }
    
    /**
     * Get current tenant address information
     */
    @GetMapping("/tenant")
    public ResponseEntity<Map<String, Object>> getTenantAddress(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        
        Map<String, Object> tenantInfo = Map.of(
            "tenantId", tenantId != null ? tenantId : "not-provided",
            "tenantType", "MULTI_TENANT",
            "address", Map.of(
                "type", "TENANT_ADDRESS",
                "street", "456 Tenant Avenue",
                "city", "Tenant City", 
                "country", "Vietnam",
                "postalCode", "200000"
            ),
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(tenantInfo);
    }
    
    /**
     * Get both user and tenant addresses
     */
    @GetMapping("/both")
    public ResponseEntity<Map<String, Object>> getBothAddresses(
            Principal principal,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        
        Map<String, Object> bothInfo = Map.of(
            "user", Map.of(
                "userId", principal != null ? principal.getName() : "anonymous",
                "userType", principal != null ? principal.getClass().getSimpleName() : "none",
                "isAuthenticated", principal != null,
                "address", Map.of(
                    "type", "USER_ADDRESS",
                    "street", "123 User Street",
                    "city", "User City",
                    "country", "Vietnam",
                    "postalCode", "100000"
                )
            ),
            "tenant", Map.of(
                "tenantId", tenantId != null ? tenantId : "not-provided",
                "address", Map.of(
                    "type", "TENANT_ADDRESS",
                    "street", "456 Tenant Avenue",
                    "city", "Tenant City",
                    "country", "Vietnam",
                    "postalCode", "200000"
                )
            ),
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(bothInfo);
    }
    
    /**
     * Test without authentication
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicAddress() {
        Map<String, Object> publicInfo = Map.of(
            "type", "PUBLIC_ADDRESS",
            "message", "This endpoint is accessible without authentication",
            "address", Map.of(
                "street", "789 Public Road",
                "city", "Public City",
                "country", "Vietnam",
                "postalCode", "300000"
            ),
            "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(publicInfo);
    }
}
