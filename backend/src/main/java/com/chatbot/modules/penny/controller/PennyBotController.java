package com.chatbot.modules.penny.controller;

import com.chatbot.modules.penny.model.PennyBot;
import com.chatbot.modules.penny.model.PennyBotType;
import com.chatbot.modules.penny.service.PennyBotManager;
import com.chatbot.modules.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Penny Bot Controller - Unified bot management API
 * Adapted từ traloitudongV2 cho chatbot-saas-v2 architecture
 */
@RestController
@RequestMapping("/api/penny/bots")
@RequiredArgsConstructor
@Slf4j
public class PennyBotController {
    
    private final PennyBotManager pennyBotManager;
    
    /**
     * Create new Penny-enhanced bot
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBot(
            @RequestBody Map<String, String> request,
            Principal principal) {
        
        // ✅ Validate tenant context
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Tenant context not found. Please provide X-Tenant-ID header"
            ));
        }
        
        UUID userId = UUID.fromString(principal.getName());
        String botName = request.get("botName");
        String botTypeStr = request.getOrDefault("botType", "CUSTOMER_SERVICE");
        String description = request.getOrDefault("botDescription", "");
        
        PennyBotType botType = PennyBotType.fromString(botTypeStr);
        
        log.info("🤖 Creating Penny bot for user: {} in tenant: {}", userId, tenantId);
        
        try {
            PennyBot createdBot = pennyBotManager.createBot(userId, botName, botType, description);
            
            return ResponseEntity.ok(Map.of(
                "botId", createdBot.getId().toString(),
                "botName", createdBot.getBotName(),
                "botType", createdBot.getBotType().name(),
                "tenantId", tenantId.toString(),
                "userId", userId,
                "status", "created",
                "message", "Penny bot created successfully"
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get all bots for current user
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getBots(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        
        log.info("📋 Getting Penny bots for user: {}", userId);
        
        List<PennyBot> bots = pennyBotManager.getBotsForUser(userId);
        
        List<Map<String, Object>> response = bots.stream()
            .map(bot -> {
                Map<String, Object> botMap = new java.util.HashMap<>();
                botMap.put("botId", bot.getId().toString());
                botMap.put("botName", bot.getBotName());
                botMap.put("botType", bot.getBotType().name());
                botMap.put("botpressBotId", bot.getBotpressBotId());
                botMap.put("isActive", bot.getIsActive());
                botMap.put("isEnabled", bot.getIsEnabled());
                botMap.put("createdAt", bot.getCreatedAt().toString());
                botMap.put("description", bot.getDescription() != null ? bot.getDescription() : "");
                return botMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all bots for current tenant
     */
    @GetMapping("/tenant")
    public ResponseEntity<List<Map<String, Object>>> getTenantBots() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(List.of());
        }
        
        log.info("📋 Getting Penny bots for tenant: {}", tenantId);
        
        List<PennyBot> bots = pennyBotManager.getBotsForTenant();
        
        List<Map<String, Object>> response = bots.stream()
            .map(bot -> {
                Map<String, Object> botMap = new java.util.HashMap<>();
                botMap.put("botId", bot.getId().toString());
                botMap.put("botName", bot.getBotName());
                botMap.put("botType", bot.getBotType().name());
                botMap.put("botpressBotId", bot.getBotpressBotId());
                botMap.put("isActive", bot.getIsActive());
                botMap.put("isEnabled", bot.getIsEnabled());
                botMap.put("createdAt", bot.getCreatedAt().toString());
                botMap.put("description", bot.getDescription() != null ? bot.getDescription() : "");
                return botMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get bot health status
     */
    @GetMapping("/{botId}/health")
    public ResponseEntity<Map<String, Object>> getBotHealth(@PathVariable String botId) {
        log.info("🏥 Getting health status for bot: {}", botId);
        
        UUID botUuid = UUID.fromString(botId);
        Map<String, Object> health = pennyBotManager.getBotHealth(botUuid);
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Update bot information
     */
    @PutMapping("/{botId}")
    public ResponseEntity<Map<String, Object>> updateBot(
            @PathVariable String botId,
            @RequestBody Map<String, Object> updates,
            Principal principal) {
        
        UUID userId = UUID.fromString(principal.getName());
        UUID botUuid = UUID.fromString(botId);
        
        log.info("📝 Updating Penny bot: {} by user: {}", botId, userId);
        
        try {
            PennyBot updatedBot = pennyBotManager.updateBot(botUuid, updates, userId);
            
            return ResponseEntity.ok(Map.of(
                "botId", updatedBot.getId().toString(),
                "botName", updatedBot.getBotName(),
                "botType", updatedBot.getBotType().name(),
                "isActive", updatedBot.getIsActive(),
                "isEnabled", updatedBot.getIsEnabled(),
                "description", updatedBot.getDescription() != null ? updatedBot.getDescription() : "",
                "message", "Penny bot updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to update Penny bot: " + e.getMessage(),
                "botId", botId
            ));
        }
    }
    
    /**
     * Toggle bot status (active/inactive)
     */
    @PutMapping("/{botId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleBotStatus(
            @PathVariable String botId,
            @RequestParam Boolean enabled,
            Principal principal) {
        
        UUID userId = UUID.fromString(principal.getName());
        UUID botUuid = UUID.fromString(botId);
        
        log.info("🔄 Toggling Penny bot: {} to {} by user: {}", botId, enabled ? "enabled" : "disabled", userId);
        
        try {
            PennyBot updatedBot = pennyBotManager.toggleBotStatus(botUuid, enabled, userId);
            
            return ResponseEntity.ok(Map.of(
                "botId", updatedBot.getId().toString(),
                "botName", updatedBot.getBotName(),
                "isActive", updatedBot.getIsActive(),
                "isEnabled", updatedBot.getIsEnabled(),
                "botType", updatedBot.getBotType().name(),
                "message", "Penny bot status updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to toggle bot status: " + e.getMessage(),
                "botId", botId
            ));
        }
    }
    
    /**
     * Get bot analytics
     */
    @GetMapping("/{botId}/analytics")
    public ResponseEntity<Map<String, Object>> getBotAnalytics(
            @PathVariable String botId,
            @RequestParam(defaultValue = "7days") String timeRange,
            Principal principal) {
        
        UUID userId = UUID.fromString(principal.getName());
        UUID botUuid = UUID.fromString(botId);
        
        log.info("📊 Getting analytics for bot: {} by user: {} with range: {}", botId, userId, timeRange);
        
        try {
            Map<String, Object> analytics = pennyBotManager.getBotAnalytics(botUuid, timeRange, userId);
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get bot analytics: " + e.getMessage(),
                "botId", botId
            ));
        }
    }
    
    /**
     * Delete bot (hard delete)
     */
    @DeleteMapping("/{botId}")
    public ResponseEntity<Map<String, String>> deleteBot(
            @PathVariable String botId,
            Principal principal) {
        
        UUID userId = UUID.fromString(principal.getName());
        UUID botUuid = UUID.fromString(botId);
        
        log.info("🗑️ Deleting Penny bot: {} by user: {}", botId, userId);
        
        try {
            Boolean success = pennyBotManager.deleteBot(botUuid, userId);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "message", "Penny bot deleted successfully",
                    "botId", botId
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to delete Penny bot - unknown reason",
                    "botId", botId
                ));
            }
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Validation error deleting bot {}: {}", botId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "botId", botId
            ));
        } catch (RuntimeException e) {
            log.error("❌ Runtime error deleting bot {}: {}", botId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Internal server error: " + e.getMessage(),
                "botId", botId
            ));
        }
    }
}
