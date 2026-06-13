package com.chatbot.core.message.usage.controller;

import com.chatbot.core.message.usage.service.MessageUsageService;
import com.chatbot.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.chatbot.core.tenant.infra.TenantContext;

@RestController
@RequestMapping("/api/message-usage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Message Usage", description = "API for checking message usage and limits")
public class MessageUsageController {

    private final MessageUsageService messageUsageService;

    /**
     * Get current message usage for tenant
     */
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current message usage", description = "Get current message usage and limits for the authenticated tenant")
    public ResponseEntity<ApiResponse<MessageUsageService.MessageUsageInfo>> getCurrentUsage() {
        try {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Tenant context not found"));
            }
            
            MessageUsageService.MessageUsageInfo usage = messageUsageService.getCurrentUsage(tenantId);
            
            return ResponseEntity.ok(ApiResponse.success(usage, "Message usage retrieved successfully"));
        } catch (Exception e) {
            log.error("❌ Error getting message usage: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error getting message usage: " + e.getMessage()));
        }
    }

    /**
     * Check if tenant can send more messages
     */
    @GetMapping("/can-send")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check if can send messages", description = "Check if tenant can send more messages based on their package limits")
    public ResponseEntity<ApiResponse<Boolean>> canSendMoreMessages() {
        try {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Tenant context not found"));
            }
            
            boolean canSend = messageUsageService.canSendMoreMessages(tenantId);
            
            return ResponseEntity.ok(ApiResponse.success(canSend, 
                    canSend ? "You can send more messages" : "Message limit reached"));
        } catch (Exception e) {
            log.error("❌ Error checking message limit: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error checking message limit: " + e.getMessage()));
        }
    }

    /**
     * Get message usage statistics for dashboard
     */
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get message statistics", description = "Get detailed message usage statistics for dashboard")
    public ResponseEntity<ApiResponse<MessageUsageService.MessageUsageStats>> getUsageStatistics() {
        try {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Tenant context not found"));
            }
            
            MessageUsageService.MessageUsageStats stats = messageUsageService.getUsageStats(tenantId);
            
            return ResponseEntity.ok(ApiResponse.success(stats, "Message statistics retrieved successfully"));
        } catch (Exception e) {
            log.error("❌ Error getting message statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error getting message statistics: " + e.getMessage()));
        }
    }

    /**
     * Get message usage for specific tenant (admin only)
     */
    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get tenant message usage", description = "Get message usage for specific tenant (Admin only)")
    public ResponseEntity<ApiResponse<MessageUsageService.MessageUsageInfo>> getTenantUsage(@PathVariable Long tenantId) {
        try {
            MessageUsageService.MessageUsageInfo usage = messageUsageService.getCurrentUsage(tenantId);
            
            return ResponseEntity.ok(ApiResponse.success(usage, "Tenant message usage retrieved successfully"));
        } catch (Exception e) {
            log.error("❌ Error getting tenant message usage: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error getting tenant message usage: " + e.getMessage()));
        }
    }

    /**
     * Get message usage statistics for specific tenant (admin only)
     */
    @GetMapping("/statistics/tenant/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get tenant message statistics", description = "Get detailed message usage statistics for specific tenant (Admin only)")
    public ResponseEntity<ApiResponse<MessageUsageService.MessageUsageStats>> getTenantStatistics(@PathVariable Long tenantId) {
        try {
            MessageUsageService.MessageUsageStats stats = messageUsageService.getUsageStats(tenantId);
            
            return ResponseEntity.ok(ApiResponse.success(stats, "Tenant message statistics retrieved successfully"));
        } catch (Exception e) {
            log.error("❌ Error getting tenant message statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error getting tenant message statistics: " + e.getMessage()));
        }
    }
}
