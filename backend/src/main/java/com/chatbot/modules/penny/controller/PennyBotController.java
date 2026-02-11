package com.chatbot.modules.penny.controller;

import com.chatbot.modules.penny.service.PennyBotManager;
import com.chatbot.modules.penny.model.PennyBot;
import com.chatbot.modules.penny.model.PennyBotType;
import com.chatbot.modules.tenant.infra.TenantContext;
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
 */
@RestController
@RequestMapping("/api/penny/bots")
@Slf4j
public class PennyBotController {
    
    private final PennyBotManager pennyBotManager;
    
    public PennyBotController(PennyBotManager pennyBotManager) {
        this.pennyBotManager = pennyBotManager;
    }
    
    /**
     * Create new Penny-enhanced bot
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBot(
            @RequestBody Map<String, String> request,
            Principal principal) {
        
        // ✅ Validate tenant context
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Tenant context not found. Please provide X-Tenant-Key header"
            ));
        }
        
        String ownerId = principal.getName();
        String botName = request.get("botName");
        String botTypeStr = request.getOrDefault("botType", "CUSTOMER_SERVICE");
        String description = request.getOrDefault("botDescription", "");
        
        PennyBotType botType = PennyBotType.fromString(botTypeStr);
        
        log.info("🤖 Creating Penny bot for owner: {} in tenant: {}", ownerId, tenantId);
        
        try {
            PennyBot createdBot = pennyBotManager.createBot(ownerId, botName, botType, description);
            
            return ResponseEntity.ok(Map.of(
                "botId", createdBot.getId().toString(),
                "botName", createdBot.getBotName(),
                "botType", createdBot.getBotType().name(),
                "tenantId", tenantId,
                "ownerId", ownerId,
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
     * Auto-create bot for Facebook connection
     */
    @PostMapping("/auto")
    public ResponseEntity<Map<String, Object>> autoCreateBot(
            @RequestBody Map<String, String> request,
            Principal principal) {
        
        String ownerId = principal.getName();
        String pageId = request.get("pageId");
        
        log.info("🤖 Auto-creating Penny bot for page: {} by owner: {}", pageId, ownerId);
        
        PennyBot createdBot = pennyBotManager.autoCreateBotForConnection(ownerId, pageId);
        
        if (createdBot != null) {
            return ResponseEntity.ok(Map.of(
                "botId", createdBot.getId().toString(),
                "pageId", pageId,
                "ownerId", ownerId,
                "botType", createdBot.getBotType().name(),
                "status", "auto-created",
                "message", "Penny bot auto-created for Facebook connection"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to auto-create Penny bot"
            ));
        }
    }
    
    /**
     * Get all bots for current owner
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getBots(Principal principal) {
        String ownerId = principal.getName();
        
        log.info("📋 Getting Penny bots for owner: {}", ownerId);
        
        List<PennyBot> bots = pennyBotManager.getBotsForOwner(ownerId);
        
        List<Map<String, Object>> response = bots.stream()
            .map(bot -> {
                Map<String, Object> botMap = new java.util.HashMap<>();
                botMap.put("botId", bot.getId().toString());
                botMap.put("botName", bot.getBotName());
                botMap.put("botType", bot.getBotType().name());
                botMap.put("botpressBotId", bot.getBotpressBotId());
                botMap.put("isActive", bot.isActive());
                botMap.put("isEnabled", bot.isEnabled());
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
        
        String ownerId = principal.getName();
        UUID botUuid = UUID.fromString(botId);
        
        log.info("📝 Updating Penny bot: {} by owner: {}", botId, ownerId);
        
        try {
            PennyBot updatedBot = pennyBotManager.updateBot(botUuid, updates, ownerId);
            
            return ResponseEntity.ok(Map.of(
                "botId", updatedBot.getId().toString(),
                "botName", updatedBot.getBotName(),
                "botType", updatedBot.getBotType().name(),
                "isActive", updatedBot.isActive(),
                "isEnabled", updatedBot.isEnabled(),
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
            @RequestParam boolean enabled,
            Principal principal) {
        
        String ownerId = principal.getName();
        UUID botUuid = UUID.fromString(botId);
        
        log.info("🔄 Toggling Penny bot: {} to {} by owner: {}", botId, enabled ? "enabled" : "disabled", ownerId);
        
        try {
            PennyBot updatedBot = pennyBotManager.toggleBotStatus(botUuid, enabled, ownerId);
            
            return ResponseEntity.ok(Map.of(
                "botId", updatedBot.getId().toString(),
                "botName", updatedBot.getBotName(),
                "isActive", updatedBot.isActive(),
                "isEnabled", updatedBot.isEnabled(),
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
        
        String ownerId = principal.getName();
        UUID botUuid = UUID.fromString(botId);
        
        log.info("📊 Getting analytics for bot: {} by owner: {} with range: {}", botId, ownerId, timeRange);
        
        try {
            Map<String, Object> analytics = pennyBotManager.getBotAnalytics(botUuid, timeRange, ownerId);
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get bot analytics: " + e.getMessage(),
                "botId", botId
            ));
        }
    }
    
    /**
     * Chat with bot
     */
    @PostMapping("/{botId}/chat")
    public ResponseEntity<Map<String, Object>> chatWithBot(
            @PathVariable String botId,
            @RequestBody Map<String, String> request,
            Principal principal) {
        
        String ownerId = principal.getName();
        UUID botUuid = UUID.fromString(botId);
        String message = request.get("message");
        
        log.info("💬 Chatting with Penny bot: {} by owner: {} - Message: {}", botId, ownerId, message);
        
        try {
            // Verify ownership
            PennyBot bot = pennyBotManager.getBot(botUuid);
            if (!bot.getOwnerId().equals(ownerId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Not authorized to chat with this bot",
                    "botId", botId
                ));
            }
            
            // Check if bot is active
            if (!bot.isActive() || !bot.isEnabled()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Bot is not active. Please activate the bot first.",
                    "botId", botId,
                    "botStatus", bot.isActive() && bot.isEnabled() ? "active" : "inactive"
                ));
            }
            
            // Process message through Penny middleware
            String botResponse = pennyBotManager.processMessage(botUuid, message, ownerId);
            
            return ResponseEntity.ok(Map.of(
                "botId", botId,
                "botName", bot.getBotName(),
                "message", message,
                "response", botResponse,
                "timestamp", java.time.LocalDateTime.now().toString(),
                "status", "success"
            ));
            
        } catch (Exception e) {
            log.error("❌ Error processing chat message for bot {}: {}", botId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to process message: " + e.getMessage(),
                "botId", botId
            ));
        }
    }
    
    /**
     * Chat with bot (public - no auth required for testing)
     */
    @PostMapping("/{botId}/chat/public")
    public ResponseEntity<Map<String, Object>> chatWithBotPublic(
            @PathVariable String botId,
            @RequestBody Map<String, String> request) {
        
        UUID botUuid = UUID.fromString(botId);
        String message = request.get("message");
        
        log.info("💬 Public chat with Penny bot: {} - Message: {}", botId, message);
        
        try {
            // Get bot without ownership check
            PennyBot bot = pennyBotManager.getBot(botUuid);
            
            // Check if bot is active
            if (!bot.isActive() || !bot.isEnabled()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Bot is not active. Please activate the bot first.",
                    "botId", botId,
                    "botStatus", bot.isActive() && bot.isEnabled() ? "active" : "inactive"
                ));
            }
            
            // Process message through Penny middleware
            String botResponse = pennyBotManager.processMessage(botUuid, message, "public");
            
            return ResponseEntity.ok(Map.of(
                "botId", botId,
                "botName", bot.getBotName(),
                "message", message,
                "response", botResponse,
                "timestamp", java.time.LocalDateTime.now().toString(),
                "status", "success"
            ));
            
        } catch (Exception e) {
            log.error("❌ Error processing public chat message for bot {}: {}", botId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to process message: " + e.getMessage(),
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
        
        String ownerId = principal.getName();
        UUID botUuid = UUID.fromString(botId);
        
        log.info("🗑️ Deleting Penny bot: {} by owner: {}", botId, ownerId);
        
        try {
            boolean success = pennyBotManager.deleteBot(botUuid, ownerId);
            
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
        } catch (IllegalStateException e) {
            log.warn("⚠️ Cannot delete bot {}: {}", botId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "botId", botId
            ));
        } catch (RuntimeException e) {
            log.error("❌ Runtime error deleting bot {}: {}", botId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage(),
                "botId", botId
            ));
        } catch (Exception e) {
            log.error("❌ Unexpected error deleting bot {}: {}", botId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Internal server error: " + e.getMessage(),
                "botId", botId
            ));
        }
    }
}
