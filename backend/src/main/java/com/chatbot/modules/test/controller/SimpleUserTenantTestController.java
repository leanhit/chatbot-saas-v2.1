package com.chatbot.modules.test.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple Test Controller for User and Tenant Information Retrieval
 * Does not depend on complex services to avoid startup issues
 */
@RestController
@RequestMapping("/api/test/simple-user-tenant")
public class SimpleUserTenantTestController {

    /**
     * Get current user information (simple version)
     */
    @GetMapping("/user/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal == null) {
            response.put("error", "User not authenticated");
            return ResponseEntity.badRequest().body(response);
        }

        response.put("principalName", principal.getName());
        response.put("principalType", principal.getClass().getSimpleName());
        response.put("isAuthenticated", true);
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Simple user info retrieved successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get user information with UserDetails
     */
    @GetMapping("/user/details")
    public ResponseEntity<Map<String, Object>> getUserDetails(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        
        if (userDetails == null) {
            response.put("error", "User not authenticated");
            return ResponseEntity.badRequest().body(response);
        }

        response.put("username", userDetails.getUsername());
        response.put("authorities", userDetails.getAuthorities());
        response.put("isAccountNonExpired", userDetails.isAccountNonExpired());
        response.put("isAccountNonLocked", userDetails.isAccountNonLocked());
        response.put("isCredentialsNonExpired", userDetails.isCredentialsNonExpired());
        response.put("isEnabled", userDetails.isEnabled());
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "User details retrieved successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get tenant information from header
     */
    @GetMapping("/tenant/from-header")
    public ResponseEntity<Map<String, Object>> getTenantFromHeader(
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
            @RequestHeader(value = "X-Tenant-Name", required = false) String tenantName) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("tenantId", tenantId != null ? tenantId : "not-provided");
        response.put("tenantName", tenantName != null ? tenantName : "not-provided");
        response.put("hasTenantId", tenantId != null);
        response.put("hasTenantName", tenantName != null);
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Tenant header information retrieved successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get comprehensive user and tenant information
     */
    @GetMapping("/comprehensive")
    public ResponseEntity<Map<String, Object>> getComprehensiveInfo(
            Principal principal,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Principal info
        if (principal != null) {
            response.put("principal", Map.of(
                "name", principal.getName(),
                "type", principal.getClass().getSimpleName()
            ));
        }
        
        // UserDetails info
        if (userDetails != null) {
            response.put("userDetails", Map.of(
                "username", userDetails.getUsername(),
                "authorities", userDetails.getAuthorities().toString(),
                "enabled", userDetails.isEnabled()
            ));
        }
        
        // Tenant info
        response.put("tenant", Map.of(
            "id", tenantId != null ? tenantId : "not-provided",
            "provided", tenantId != null
        ));
        
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Comprehensive user and tenant info retrieved successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint without authentication
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicInfo() {
        return ResponseEntity.ok(Map.of(
            "message", "This is a public endpoint - no authentication required",
            "service", "SimpleUserTenantTestController",
            "timestamp", System.currentTimeMillis(),
            "endpoints", List.of(
                "/api/test/simple-user-tenant/user/current",
                "/api/test/simple-user-tenant/user/details",
                "/api/test/simple-user-tenant/tenant/from-header",
                "/api/test/simple-user-tenant/comprehensive",
                "/api/test/simple-user-tenant/public"
            )
        ));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "SimpleUserTenantTestController",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
