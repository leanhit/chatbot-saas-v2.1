package com.chatbot.shared.penny.rules;

import com.chatbot.core.tenant.infra.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * Custom Logic Controller - API quản lý custom logic cho bot
 */
@RestController
@RequestMapping("/api/penny/bots/{botId}/custom-logic")
@Slf4j
public class CustomLogicController {

    private final CustomLogicEngine customLogicEngine;
    private final BotRuleManager botRuleManager;
    private final ResponseTemplateManager responseTemplateManager;

    public CustomLogicController(CustomLogicEngine customLogicEngine,
                                BotRuleManager botRuleManager,
                                ResponseTemplateManager responseTemplateManager) {
        this.customLogicEngine = customLogicEngine;
        this.botRuleManager = botRuleManager;
        this.responseTemplateManager = responseTemplateManager;
    }

    /**
     * Get custom logic overview
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCustomLogicOverview(@PathVariable String botId) {
        UUID botUuid = UUID.fromString(botId);
        
        CustomLogicEngine.CustomLogicStatistics stats = customLogicEngine.getCustomLogicStatistics(botUuid);
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("hasCustomLogic", stats.isHasCustomLogic());
        response.put("totalRules", stats.getTotalRules());
        response.put("activeRules", stats.getActiveRules());
        response.put("totalTemplates", stats.getTotalTemplates());
        response.put("activeTemplates", stats.getActiveTemplates());
        response.put("totalRuleExecutions", stats.getTotalRuleExecutions());
        response.put("totalTemplateUsage", stats.getTotalTemplateUsage());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get detailed statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getDetailedStatistics(@PathVariable String botId) {
        UUID botUuid = UUID.fromString(botId);
        
        // Get rule statistics
        var ruleStats = botRuleManager.getRulesForBot(botUuid);
        Map<String, Long> rulesByType = ruleStats.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                r -> r.getRuleType().name(),
                java.util.stream.Collectors.counting()
            ));
        
        Map<String, Long> rulesByTriggerType = ruleStats.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                r -> r.getTriggerType().name(),
                java.util.stream.Collectors.counting()
            ));
        
        // Get template statistics
        ResponseTemplateManager.TemplateStatistics templateStats = 
            responseTemplateManager.getTemplateStatistics(botUuid);
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("ruleStatistics", Map.of(
            "totalRules", ruleStats.size(),
            "activeRules", ruleStats.stream().mapToLong(r -> r.getIsActive() ? 1 : 0).sum(),
            "totalExecutions", ruleStats.stream().mapToLong(BotRule::getExecutionCount).sum(),
            "rulesByType", rulesByType,
            "rulesByTriggerType", rulesByTriggerType
        ));
        
        response.put("templateStatistics", Map.of(
            "totalTemplates", templateStats.getTotalTemplates(),
            "activeTemplates", templateStats.getTotalTemplates() - 
                responseTemplateManager.getTemplatesForBot(botUuid).stream()
                    .mapToLong(t -> t.getIsActive() ? 0 : 1).sum(),
            "totalUsage", templateStats.getTotalUsage(),
            "usageByIntent", templateStats.getUsageByIntent()
        ));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Enable/disable custom logic
     */
    @PutMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleCustomLogic(
            @PathVariable String botId,
            @RequestBody Map<String, Boolean> request,
            Principal principal) {
        
        // Validate tenant context
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Tenant context not found. Please provide X-Tenant-Key header"
            ));
        }
        
        Boolean enabled = request.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "enabled field is required"
            ));
        }
        
        UUID botUuid = UUID.fromString(botId);
        boolean success = customLogicEngine.toggleCustomLogic(botUuid, enabled);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "message", "Custom logic " + (enabled ? "enabled" : "disabled") + " successfully",
                "botId", botId,
                "enabled", enabled
            ));
        } else {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to toggle custom logic"
            ));
        }
    }

    /**
     * Test custom logic with sample data
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testCustomLogic(
            @PathVariable String botId,
            @RequestBody Map<String, Object> testData) {
        
        UUID botUuid = UUID.fromString(botId);
        
        try {
            // Create mock request and context
            com.chatbot.shared.penny.dto.request.MiddlewareRequest mockRequest = 
                com.chatbot.shared.penny.dto.request.MiddlewareRequest.builder()
                    .requestId("test-" + System.currentTimeMillis())
                    .userId((String) testData.getOrDefault("userId", "test-user"))
                    .platform((String) testData.getOrDefault("platform", "test"))
                    .message((String) testData.getOrDefault("message", ""))
                    .botId(botId)
                    .tenantId(1L)
                    .ownerId("test-owner")
                    .language((String) testData.getOrDefault("language", "vi"))
                    .build();
            
            com.chatbot.shared.penny.context.ConversationContext mockContext = 
                com.chatbot.shared.penny.context.ConversationContext.builder()
                    .contextId("test-context")
                    .botId(botId)
                    .userId((String) testData.getOrDefault("userId", "test-user"))
                    .platform((String) testData.getOrDefault("platform", "test"))
                    .language((String) testData.getOrDefault("language", "vi"))
                    .metadata((Map<String, Object>) testData.getOrDefault("context", Map.of()))
                    .build();
            
            com.chatbot.shared.penny.routing.dto.IntentAnalysisResult mockIntent = 
                com.chatbot.shared.penny.routing.dto.IntentAnalysisResult.builder()
                    .primaryIntent((String) testData.getOrDefault("intent", "greeting"))
                    .confidence((Double) testData.getOrDefault("confidence", 0.8))
                    .build();
            
            // Process with custom logic
            com.chatbot.shared.penny.dto.response.MiddlewareResponse response = 
                customLogicEngine.processWithCustomLogic(mockRequest, mockContext, mockIntent);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("testData", testData);
            
            if (response != null) {
                result.put("matched", true);
                result.put("response", response.getResponse());
                result.put("providerUsed", response.getProviderUsed());
                result.put("selectionReason", response.getProcessingMetrics().getSelectionReason());
                result.put("confidence", response.getProcessingMetrics().getConfidence());
            } else {
                result.put("matched", false);
                result.put("message", "No custom logic matched");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ Error testing custom logic: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error testing custom logic: " + e.getMessage()
            ));
        }
    }

    /**
     * Export custom logic configuration
     */
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportCustomLogic(@PathVariable String botId) {
        UUID botUuid = UUID.fromString(botId);
        
        try {
            // Export rules
            var rules = botRuleManager.getRulesForBot(botUuid);
            var exportedRules = rules.stream()
                .map(rule -> {
                    Map<String, Object> ruleData = new java.util.HashMap<>();
                    ruleData.put("name", rule.getName());
                    ruleData.put("description", rule.getDescription());
                    ruleData.put("ruleType", rule.getRuleType().name());
                    ruleData.put("triggerType", rule.getTriggerType().name());
                    ruleData.put("triggerValue", rule.getTriggerValue());
                    ruleData.put("condition", rule.getCondition());
                    ruleData.put("action", rule.getAction());
                    ruleData.put("priority", rule.getPriority());
                    ruleData.put("isActive", rule.getIsActive());
                    return ruleData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            // Export templates
            var templates = responseTemplateManager.getTemplatesForBot(botUuid);
            var exportedTemplates = templates.stream()
                .map(template -> {
                    Map<String, Object> templateData = new java.util.HashMap<>();
                    templateData.put("name", template.getName());
                    templateData.put("description", template.getDescription());
                    templateData.put("intent", template.getIntent());
                    templateData.put("templateText", template.getTemplateText());
                    templateData.put("templateType", template.getTemplateType().name());
                    templateData.put("language", template.getLanguage());
                    templateData.put("priority", template.getPriority());
                    templateData.put("isActive", template.getIsActive());
                    return templateData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> export = new java.util.HashMap<>();
            export.put("botId", botId);
            export.put("exportedAt", java.time.Instant.now().toString());
            export.put("rules", exportedRules);
            export.put("templates", exportedTemplates);
            
            return ResponseEntity.ok(export);
            
        } catch (Exception e) {
            log.error("❌ Error exporting custom logic: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error exporting custom logic: " + e.getMessage()
            ));
        }
    }

    /**
     * Import custom logic configuration
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importCustomLogic(
            @PathVariable String botId,
            @RequestBody Map<String, Object> importData,
            Principal principal) {
        
        // Validate tenant context
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Tenant context not found. Please provide X-Tenant-Key header"
            ));
        }
        
        UUID botUuid = UUID.fromString(botId);
        String createdBy = principal.getName();
        
        try {
            int importedRules = 0;
            int importedTemplates = 0;
            
            // Import rules
            if (importData.containsKey("rules")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> rules = 
                    (java.util.List<Map<String, Object>>) importData.get("rules");
                
                for (Map<String, Object> ruleData : rules) {
                    try {
                        botRuleManager.createRule(
                            botUuid,
                            (String) ruleData.get("name"),
                            (String) ruleData.get("description"),
                            (String) ruleData.get("condition"),
                            (String) ruleData.get("action"),
                            BotRule.RuleType.valueOf((String) ruleData.get("ruleType")),
                            BotRule.TriggerType.valueOf((String) ruleData.get("triggerType")),
                            (String) ruleData.get("triggerValue"),
                            (Integer) ruleData.get("priority"),
                            createdBy
                        );
                        importedRules++;
                    } catch (Exception e) {
                        log.warn("⚠️ Failed to import rule: {}", e.getMessage());
                    }
                }
            }
            
            // Import templates
            if (importData.containsKey("templates")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> templates = 
                    (java.util.List<Map<String, Object>>) importData.get("templates");
                
                for (Map<String, Object> templateData : templates) {
                    try {
                        responseTemplateManager.createTemplate(
                            botUuid,
                            (String) templateData.get("name"),
                            (String) templateData.get("description"),
                            (String) templateData.get("intent"),
                            (String) templateData.get("templateText"),
                            ResponseTemplate.TemplateType.valueOf((String) templateData.get("templateType")),
                            (String) templateData.get("language"),
                            (Integer) templateData.get("priority"),
                            (Map<String, Object>) templateData.get("variables"),
                            (java.util.List<Map<String, Object>>) templateData.get("quickReplies"),
                            createdBy
                        );
                        importedTemplates++;
                    } catch (Exception e) {
                        log.warn("⚠️ Failed to import template: {}", e.getMessage());
                    }
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Custom logic imported successfully",
                "importedRules", importedRules,
                "importedTemplates", importedTemplates,
                "botId", botId
            ));
            
        } catch (Exception e) {
            log.error("❌ Error importing custom logic: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error importing custom logic: " + e.getMessage()
            ));
        }
    }
}
