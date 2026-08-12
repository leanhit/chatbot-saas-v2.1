package com.chatbot.shared.penny.controller;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.shared.penny.service.PennyBotManager;
import com.chatbot.shared.penny.model.PennyBot;
import com.chatbot.shared.penny.model.PennyBotType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Penny Bot Controller - Unified bot management API
 * All exceptions bubble up to GlobalExceptionHandler for standardized ErrorResponse output.
 */
@RestController
@RequestMapping("/api/penny/bots")
@Slf4j
public class PennyBotController {

    private final PennyBotManager pennyBotManager;
    private final TenantPermissionValidator tenantPermissionValidator;

    public PennyBotController(PennyBotManager pennyBotManager, TenantPermissionValidator tenantPermissionValidator) {
        this.pennyBotManager = pennyBotManager;
        this.tenantPermissionValidator = tenantPermissionValidator;
    }
    
    private Long getValidatedTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not found. Please provide X-Tenant-Key header");
        }
        return tenantId;
    }

    private void validateOwnerOrEditor(Long tenantId, String userEmail, String action) {
        if (!tenantPermissionValidator.isOwnerOrEditor(tenantId, userEmail)) {
            throw new InsufficientPermissionException("Only OWNER or EDITOR can " + action);
        }
    }

    /**
     * Create new Penny-enhanced bot (OWNER or EDITOR only)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBot(
            @RequestBody Map<String, String> request,
            Principal principal) {

        Long tenantId = getValidatedTenantId();
        String ownerId = principal.getName();
        validateOwnerOrEditor(tenantId, ownerId, "create bots");

        String botName = request.get("botName");
        String botTypeStr = request.getOrDefault("botType", "CUSTOMER_SERVICE");
        String description = request.getOrDefault("botDescription", "");

        PennyBotType botType = PennyBotType.fromString(botTypeStr);

        log.info("🤖 Creating Penny bot for owner: {} in tenant: {}", ownerId, tenantId);

        PennyBot createdBot = pennyBotManager.createBot(ownerId, botName, botType, description);

        return ResponseEntity.ok(Map.of(
            "botId", createdBot.getId().toString(),
            "botName", createdBot.getBotName(),
            "botType", createdBot.getBotType().name(),
            "pennyBotId", createdBot.getPennyBotId(),
            "tenantId", tenantId,
            "ownerId", ownerId,
            "status", "created",
            "message", "Penny bot created successfully"
        ));
    }
    
    /**
     * Auto-create bot for Facebook connection (OWNER or EDITOR only)
     */
    @PostMapping("/auto")
    public ResponseEntity<Map<String, Object>> autoCreateBot(
            @RequestBody Map<String, String> request,
            Principal principal) {

        Long tenantId = getValidatedTenantId();
        String ownerId = principal.getName();
        validateOwnerOrEditor(tenantId, ownerId, "auto-create bots");

        String pageId = request.get("pageId");

        log.info("🤖 Auto-creating Penny bot for page: {} by owner: {}", pageId, ownerId);

        PennyBot createdBot = pennyBotManager.autoCreateBotForConnection(ownerId, pageId);

        return ResponseEntity.ok(Map.of(
            "botId", createdBot.getId().toString(),
            "pageId", pageId,
            "ownerId", ownerId,
            "botType", createdBot.getBotType().name(),
            "status", "auto-created",
            "message", "Penny bot auto-created for Facebook connection"
        ));
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
                botMap.put("pennyBotId", bot.getPennyBotId());
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
     * Get single bot by ID
     */
    @GetMapping("/{botId}")
    public ResponseEntity<Map<String, Object>> getBotById(@PathVariable String botId) {
        log.info("🔍 Getting bot details for ID: {}", botId);
        
        UUID botUuid = UUID.fromString(botId);
        PennyBot bot = pennyBotManager.getBot(botUuid);
        
        Map<String, Object> botMap = new java.util.HashMap<>();
        botMap.put("botId", bot.getId().toString());
        botMap.put("botName", bot.getBotName());
        botMap.put("botType", bot.getBotType().name());
        botMap.put("pennyBotId", bot.getPennyBotId());
        botMap.put("isActive", bot.isActive());
        botMap.put("isEnabled", bot.isEnabled());
        botMap.put("createdAt", bot.getCreatedAt().toString());
        botMap.put("description", bot.getDescription() != null ? bot.getDescription() : "");
        
        return ResponseEntity.ok(botMap);
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
     * Update bot information (OWNER or EDITOR only)
     */
    @PutMapping("/{botId}")
    public ResponseEntity<Map<String, Object>> updateBot(
            @PathVariable String botId,
            @RequestBody Map<String, Object> updates,
            Principal principal) {

        Long tenantId = getValidatedTenantId();
        String ownerId = principal.getName();
        validateOwnerOrEditor(tenantId, ownerId, "update bots");

        UUID botUuid = UUID.fromString(botId);

        log.info("📝 Updating Penny bot: {} by owner: {}", botId, ownerId);

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
    }
    
    /**
     * Toggle bot status (active/inactive) (OWNER or EDITOR only)
     */
    @PutMapping("/{botId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleBotStatus(
            @PathVariable String botId,
            @RequestParam boolean enabled,
            Principal principal) {

        Long tenantId = getValidatedTenantId();
        String ownerId = principal.getName();
        validateOwnerOrEditor(tenantId, ownerId, "toggle bot status");

        UUID botUuid = UUID.fromString(botId);

        log.info("🔄 Toggling Penny bot: {} to {} by owner: {}", botId, enabled ? "enabled" : "disabled", ownerId);

        PennyBot updatedBot = pennyBotManager.toggleBotStatus(botUuid, enabled, ownerId);

        return ResponseEntity.ok(Map.of(
            "botId", updatedBot.getId().toString(),
            "botName", updatedBot.getBotName(),
            "isActive", updatedBot.isActive(),
            "isEnabled", updatedBot.isEnabled(),
            "botType", updatedBot.getBotType().name(),
            "message", "Penny bot status updated successfully"
        ));
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
        
        Map<String, Object> analytics = pennyBotManager.getBotAnalytics(botUuid, timeRange, ownerId);
        return ResponseEntity.ok(analytics);
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
        boolean isTestMode = "true".equals(request.get("testMode"));
        
        log.info("💬 Chatting with Penny bot: {} by owner: {} - Message: {} - TestMode: {}", botId, ownerId, message, isTestMode);
        
        PennyBot bot = pennyBotManager.getBot(botUuid);
        if (!bot.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Not authorized to chat with this bot");
        }
        
        if (!bot.isActive() || !bot.isEnabled()) {
            throw new IllegalStateException("Bot is not active. Please activate the bot first.");
        }
        
        String botResponse = pennyBotManager.processMessage(botUuid, message, ownerId, isTestMode);
        
        return ResponseEntity.ok(Map.of(
            "botId", botId,
            "botName", bot.getBotName(),
            "message", message,
            "response", botResponse,
            "testMode", isTestMode,
            "timestamp", java.time.LocalDateTime.now().toString(),
            "status", "success"
        ));
    }
    
    /**
     * Chat with bot (public - requires API key authentication)
     */
    @PostMapping("/{botId}/chat/public")
    public ResponseEntity<Map<String, Object>> chatWithBotPublic(
            @PathVariable String botId,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Public-API-Key", required = false) String apiKey) {
        
        if (!isValidPublicApiKey(apiKey)) {
            log.warn("⚠️ Invalid or missing API key for public chat with bot: {}", botId);
            throw new AccessDeniedException("Invalid or missing API key");
        }
        
        UUID botUuid = UUID.fromString(botId);
        String message = request.get("message");
        
        log.info("💬 Public chat with Penny bot: {} - Message: {}", botId, message);
        
        PennyBot bot = pennyBotManager.getBot(botUuid);
        
        if (!bot.isActive() || !bot.isEnabled()) {
            throw new IllegalStateException("Bot is not active. Please activate the bot first.");
        }
        
        String botResponse = pennyBotManager.processMessage(botUuid, message, "public", false);
        
        return ResponseEntity.ok(Map.of(
            "botId", botId,
            "botName", bot.getBotName(),
            "message", message,
            "response", botResponse,
            "timestamp", java.time.LocalDateTime.now().toString(),
            "status", "success"
        ));
    }
    
    private boolean isValidPublicApiKey(String apiKey) {
        String expectedKey = System.getenv("PENNY_PUBLIC_API_KEY");
        if (expectedKey == null || expectedKey.isBlank()) {
            log.warn("⚠️ PENNY_PUBLIC_API_KEY not configured, public endpoint disabled");
            return false;
        }
        return expectedKey.equals(apiKey);
    }
    
    /**
     * Delete bot (hard delete) (OWNER or EDITOR only)
     */
    @DeleteMapping("/{botId}")
    public ResponseEntity<Map<String, String>> deleteBot(
            @PathVariable String botId,
            Principal principal) {

        Long tenantId = getValidatedTenantId();
        String ownerId = principal.getName();
        validateOwnerOrEditor(tenantId, ownerId, "delete bots");

        UUID botUuid = UUID.fromString(botId);

        log.info("🗑️ Deleting Penny bot: {} by owner: {}", botId, ownerId);

        boolean success = pennyBotManager.deleteBot(botUuid, ownerId);

        if (success) {
            return ResponseEntity.ok(Map.of(
                "message", "Penny bot deleted successfully",
                "botId", botId
            ));
        } else {
            throw new IllegalStateException("Failed to delete Penny bot - unknown reason");
        }
    }
}
