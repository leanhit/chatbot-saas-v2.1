package com.chatbot.core.tenant.controller;

import com.chatbot.core.tenant.service.PackageLimitValidationService;
import com.chatbot.shared.constants.ApiConstants;
import com.chatbot.shared.dto.ApiResponse;
import com.chatbot.shared.exceptions.BaseException;
import com.chatbot.shared.exceptions.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/package-limits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Package Limits", description = "API for checking package limits and chatbot restrictions")
public class PackageLimitController {

    private final PackageLimitValidationService limitValidationService;

    /**
     * Get chatbot limit information for current tenant
     */
    @GetMapping("/chatbot-info")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get chatbot limit info", description = "Get current chatbot usage and limits for the authenticated tenant")
    public ResponseEntity<ApiResponse<PackageLimitValidationService.ChatbotLimitInfo>> getChatbotLimitInfo() {
        try {
            Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
            if (tenantId == null) {
                throw new BaseException(ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
            }
            
            PackageLimitValidationService.ChatbotLimitInfo limitInfo = 
                    limitValidationService.getChatbotLimitInfo(tenantId);
            
            return ResponseEntity.ok(ApiResponse.success(limitInfo, "Chatbot limit information retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting chatbot limit info", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error getting chatbot limit info: " + e.getMessage()));
        }
    }

    /**
     * Get chatbot limit information for specific tenant
     */
    @GetMapping("/chatbot-info/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get chatbot limit info by tenant", description = "Get current chatbot usage and limits for a specific tenant (Admin only)")
    public ResponseEntity<ApiResponse<PackageLimitValidationService.ChatbotLimitInfo>> getChatbotLimitInfoByTenant(
            @PathVariable Long tenantId) {
        try {
            PackageLimitValidationService.ChatbotLimitInfo limitInfo = 
                    limitValidationService.getChatbotLimitInfo(tenantId);
            
            return ResponseEntity.ok(ApiResponse.success(limitInfo, "Chatbot limit information retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting chatbot limit info for tenant {}", tenantId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error getting chatbot limit info: " + e.getMessage()));
        }
    }

    /**
     * Check if tenant can create more chatbots
     */
    @GetMapping("/can-create-chatbot/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Check if can create chatbot", description = "Check if tenant can create more chatbots (Admin only)")
    public ResponseEntity<ApiResponse<Boolean>> canCreateMoreChatbots(@PathVariable Long tenantId) {
        try {
            boolean canCreate = limitValidationService.canCreateMoreChatbots(tenantId);
            
            return ResponseEntity.ok(ApiResponse.success(canCreate, 
                    canCreate ? "Tenant can create more chatbots" : "Tenant has reached chatbot limit"));
        } catch (Exception e) {
            log.error("Error checking chatbot creation limit for tenant {}", tenantId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error checking chatbot limit: " + e.getMessage()));
        }
    }

    /**
     * Get remaining chatbot slots for tenant
     */
    @GetMapping("/remaining-slots/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get remaining chatbot slots", description = "Get number of remaining chatbot slots for tenant (Admin only)")
    public ResponseEntity<ApiResponse<Integer>> getRemainingChatbotSlots(@PathVariable Long tenantId) {
        try {
            int remainingSlots = limitValidationService.getRemainingChatbotSlots(tenantId);
            
            return ResponseEntity.ok(ApiResponse.success(remainingSlots, "Remaining chatbot slots retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting remaining chatbot slots for tenant {}", tenantId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error getting remaining slots: " + e.getMessage()));
        }
    }

    /**
     * Validate chatbot creation (will throw exception if limit exceeded)
     */
    @PostMapping("/validate-chatbot-creation/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Validate chatbot creation", description = "Validate if tenant can create chatbot (Admin only)")
    public ResponseEntity<ApiResponse<String>> validateChatbotCreation(@PathVariable Long tenantId) {
        try {
            limitValidationService.validateChatbotCreation(tenantId);
            
            return ResponseEntity.ok(ApiResponse.success("OK", "Tenant can create chatbots"));
        } catch (Exception e) {
            log.warn("Chatbot creation validation failed for tenant {}: {}", tenantId, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
