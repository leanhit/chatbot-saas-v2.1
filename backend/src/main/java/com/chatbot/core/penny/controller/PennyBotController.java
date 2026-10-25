package com.chatbot.core.penny.controller;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.core.penny.service.PennyBotManager;
import com.chatbot.core.penny.model.PennyBot;
import com.chatbot.core.penny.model.PennyBotType;
import com.chatbot.core.penny.rules.BotRuleManager;
import com.chatbot.core.penny.rules.ResponseTemplateManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
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
    private final BotRuleManager botRuleManager;
    private final ResponseTemplateManager responseTemplateManager;

    public PennyBotController(PennyBotManager pennyBotManager, 
                              TenantPermissionValidator tenantPermissionValidator,
                              BotRuleManager botRuleManager,
                              ResponseTemplateManager responseTemplateManager) {
        this.pennyBotManager = pennyBotManager;
        this.tenantPermissionValidator = tenantPermissionValidator;
        this.botRuleManager = botRuleManager;
        this.responseTemplateManager = responseTemplateManager;
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

    /**
     * Export full bot configuration (AI settings + rules + templates)
     * This is for PennyBotConfig page - exports complete bot configuration
     */
    @GetMapping("/{botId}/config/export")
    public ResponseEntity<Map<String, Object>> exportFullBotConfig(
            @PathVariable String botId,
            Principal principal) {

        Long tenantId = getValidatedTenantId();
        String ownerId = principal.getName();

        UUID botUuid = UUID.fromString(botId);
        PennyBot bot = pennyBotManager.getBot(botUuid);

        if (!bot.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Not authorized to export this bot configuration");
        }

        log.info("📦 Exporting full bot configuration for: {}", botId);

        // Export bot settings
        Map<String, Object> botConfig = new java.util.HashMap<>();
        botConfig.put("botId", bot.getId().toString());
        botConfig.put("botName", bot.getBotName());
        botConfig.put("botType", bot.getBotType().name());
        botConfig.put("businessName", bot.getBusinessName() != null ? bot.getBusinessName() : "");
        botConfig.put("businessDescription", bot.getBusinessDescription() != null ? bot.getBusinessDescription() : "");
        botConfig.put("systemPrompt", bot.getSystemPrompt() != null ? bot.getSystemPrompt() : "");
        botConfig.put("confidenceThreshold", bot.getConfidenceThreshold());
        botConfig.put("providerType", bot.getProviderType() != null ? bot.getProviderType().name() : "OPENAI");
        botConfig.put("modelName", bot.getModelName() != null ? bot.getModelName() : "gpt-4o-mini");
        botConfig.put("temperature", bot.getTemperature() != null ? bot.getTemperature() : 0.7f);
        botConfig.put("personaStyle", bot.getPersonaStyle() != null ? bot.getPersonaStyle().name() : "PROFESSIONAL");
        botConfig.put("customInstructions", bot.getCustomInstructions() != null ? bot.getCustomInstructions() : "");
        botConfig.put("greetingMessage", bot.getGreetingMessage() != null ? bot.getGreetingMessage() : "");
        botConfig.put("fallbackMessage", bot.getFallbackMessage() != null ? bot.getFallbackMessage() : "");
        botConfig.put("configuration", bot.getConfiguration() != null ? bot.getConfiguration() : "");

        // Export rules
        List<Map<String, Object>> rules = botRuleManager.getRulesForBot(botUuid).stream()
            .map(rule -> {
                Map<String, Object> ruleMap = new java.util.HashMap<>();
                ruleMap.put("id", rule.getId().toString());
                ruleMap.put("name", rule.getName());
                ruleMap.put("description", rule.getDescription() != null ? rule.getDescription() : "");
                ruleMap.put("ruleType", rule.getRuleType().name());
                ruleMap.put("triggerType", rule.getTriggerType().name());
                ruleMap.put("triggerValue", rule.getTriggerValue() != null ? rule.getTriggerValue() : "");
                ruleMap.put("condition", rule.getCondition() != null ? rule.getCondition() : "");
                ruleMap.put("action", rule.getAction() != null ? rule.getAction() : "");
                ruleMap.put("priority", rule.getPriority());
                ruleMap.put("isActive", rule.getIsActive());
                return ruleMap;
            })
            .collect(Collectors.toList());

        // Export templates
        List<Map<String, Object>> templates = responseTemplateManager.getTemplatesForBot(botUuid).stream()
            .map(template -> {
                Map<String, Object> templateMap = new java.util.HashMap<>();
                templateMap.put("id", template.getId().toString());
                templateMap.put("name", template.getName());
                templateMap.put("description", template.getDescription() != null ? template.getDescription() : "");
                templateMap.put("intent", template.getIntent() != null ? template.getIntent() : "");
                templateMap.put("templateText", template.getTemplateText() != null ? template.getTemplateText() : "");
                templateMap.put("templateType", template.getTemplateType().name());
                templateMap.put("language", template.getLanguage() != null ? template.getLanguage() : "vi");
                templateMap.put("priority", template.getPriority());
                templateMap.put("variables", template.getVariables() != null ? template.getVariables() : Map.of());
                templateMap.put("quickReplies", template.getQuickReplies() != null ? template.getQuickReplies() : List.of());
                templateMap.put("isActive", template.getIsActive());
                return templateMap;
            })
            .collect(Collectors.toList());

        Map<String, Object> exportData = Map.of(
            "version", "2.1",
            "exportedAt", java.time.LocalDateTime.now().toString(),
            "botConfig", botConfig,
            "rules", rules,
            "templates", templates,
            "rulesCount", rules.size(),
            "templatesCount", templates.size()
        );

        return ResponseEntity.ok(exportData);
    }

    /**
     * Import full bot configuration (AI settings + rules + templates)
     * This is for PennyBotConfig page - imports complete bot configuration
     */
    @PostMapping("/{botId}/config/import")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Map<String, Object>> importFullBotConfig(
            @PathVariable String botId,
            @RequestBody Map<String, Object> importData,
            Principal principal) {

        Long tenantId = getValidatedTenantId();
        String ownerId = principal.getName();
        validateOwnerOrEditor(tenantId, ownerId, "import bot configuration");

        UUID botUuid = UUID.fromString(botId);
        PennyBot bot = pennyBotManager.getBot(botUuid);

        if (!bot.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Not authorized to import this bot configuration");
        }

        log.info("📥 Importing full bot configuration for: {}", botId);

        int importedRules = 0;
        int importedTemplates = 0;
        int errors = 0;

        try {
            // Import bot settings
            @SuppressWarnings("unchecked")
            Map<String, Object> botConfig = (Map<String, Object>) importData.get("botConfig");
            if (botConfig != null) {
                if (botConfig.containsKey("businessName")) {
                    bot.setBusinessName((String) botConfig.get("businessName"));
                }
                if (botConfig.containsKey("businessDescription")) {
                    bot.setBusinessDescription((String) botConfig.get("businessDescription"));
                }
                if (botConfig.containsKey("systemPrompt")) {
                    bot.setSystemPrompt((String) botConfig.get("systemPrompt"));
                }
                if (botConfig.containsKey("confidenceThreshold")) {
                    bot.setConfidenceThreshold(((Number) botConfig.get("confidenceThreshold")).floatValue());
                }
                if (botConfig.containsKey("providerType")) {
                    String providerType = (String) botConfig.get("providerType");
                    try {
                        bot.setProviderType(com.chatbot.core.penny.model.LlmProviderType.valueOf(providerType));
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid provider type: {}, keeping current", providerType);
                    }
                }
                if (botConfig.containsKey("modelName")) {
                    bot.setModelName((String) botConfig.get("modelName"));
                }
                if (botConfig.containsKey("temperature")) {
                    bot.setTemperature(((Number) botConfig.get("temperature")).floatValue());
                }
                if (botConfig.containsKey("personaStyle")) {
                    String personaStyle = (String) botConfig.get("personaStyle");
                    try {
                        bot.setPersonaStyle(com.chatbot.core.penny.model.BotPersonaStyle.valueOf(personaStyle));
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid persona style: {}, keeping current", personaStyle);
                    }
                }
                if (botConfig.containsKey("customInstructions")) {
                    bot.setCustomInstructions((String) botConfig.get("customInstructions"));
                }
                if (botConfig.containsKey("greetingMessage")) {
                    bot.setGreetingMessage((String) botConfig.get("greetingMessage"));
                }
                if (botConfig.containsKey("fallbackMessage")) {
                    bot.setFallbackMessage((String) botConfig.get("fallbackMessage"));
                }
                if (botConfig.containsKey("configuration")) {
                    bot.setConfiguration((String) botConfig.get("configuration"));
                }

                pennyBotManager.updateBot(botUuid, botConfig, ownerId);
                log.info("✅ Bot settings imported successfully");
            }

            // Import rules (using CustomLogicController's import logic)
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rules = (List<Map<String, Object>>) importData.get("rules");
            if (rules != null) {
                for (Map<String, Object> ruleData : rules) {
                    try {
                        // Create rule using BotRuleManager
                        String name = (String) ruleData.get("name");
                        String description = (String) ruleData.get("description");
                        String ruleType = (String) ruleData.get("ruleType");
                        String triggerType = (String) ruleData.get("triggerType");
                        String triggerValue = (String) ruleData.get("triggerValue");
                        String condition = (String) ruleData.get("condition");
                        String action = (String) ruleData.get("action");
                        Integer priority = ruleData.containsKey("priority") ? ((Number) ruleData.get("priority")).intValue() : 0;
                        Boolean isActive = ruleData.containsKey("isActive") ? (Boolean) ruleData.get("isActive") : true;

                        botRuleManager.createRule(
                            botUuid,
                            name,
                            description,
                            condition,
                            action,
                            com.chatbot.core.penny.rules.BotRule.RuleType.valueOf(ruleType),
                            com.chatbot.core.penny.rules.BotRule.TriggerType.valueOf(triggerType),
                            triggerValue,
                            priority,
                            ownerId
                        );
                        importedRules++;
                    } catch (Exception e) {
                        log.error("❌ Failed to import rule: {}", e.getMessage());
                        errors++;
                    }
                }
                log.info("✅ Imported {} rules", importedRules);
            }

            // Import templates
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> templates = (List<Map<String, Object>>) importData.get("templates");
            if (templates != null) {
                for (Map<String, Object> templateData : templates) {
                    try {
                        String name = (String) templateData.get("name");
                        String description = (String) templateData.get("description");
                        String intent = (String) templateData.get("intent");
                        String templateText = (String) templateData.get("templateText");
                        String templateType = (String) templateData.get("templateType");
                        String language = (String) templateData.get("language");
                        Integer priority = templateData.containsKey("priority") ? ((Number) templateData.get("priority")).intValue() : 0;

                        @SuppressWarnings("unchecked")
                        Map<String, Object> variables = templateData.containsKey("variables")
                            ? (Map<String, Object>) templateData.get("variables")
                            : Map.of();

                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> quickReplies = templateData.containsKey("quickReplies")
                            ? (List<Map<String, Object>>) templateData.get("quickReplies")
                            : List.of();

                        responseTemplateManager.createTemplate(
                            botUuid,
                            name,
                            description,
                            intent,
                            templateText,
                            com.chatbot.core.penny.rules.ResponseTemplate.TemplateType.valueOf(templateType),
                            language,
                            priority,
                            variables,
                            quickReplies,
                            ownerId
                        );
                        importedTemplates++;
                    } catch (Exception e) {
                        log.error("❌ Failed to import template: {}", e.getMessage());
                        errors++;
                    }
                }
                log.info("✅ Imported {} templates", importedTemplates);
            }

            return ResponseEntity.ok(Map.of(
                "message", "Bot configuration imported successfully",
                "importedRules", importedRules,
                "importedTemplates", importedTemplates,
                "errors", errors,
                "status", errors == 0 ? "success" : "partial_success"
            ));

        } catch (Exception e) {
            log.error("❌ Failed to import bot configuration: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to import bot configuration: " + e.getMessage());
        }
    }
}
