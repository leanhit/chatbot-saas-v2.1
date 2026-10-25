package com.chatbot.core.penny.rules;

import com.chatbot.core.penny.rules.dto.BotpressImportDto;
import com.chatbot.core.tenant.infra.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
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
    private final ImportExportChangelogRepository changelogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Import strategy enum
     */
    public enum ImportStrategy {
        OVERWRITE,  // Replace existing rules/templates with same name
        SKIP,       // Skip existing rules/templates, only import new ones
        MERGE,      // Merge with existing (update if exists, create if new)
        APPEND      // Only add new, never update existing
    }

    public CustomLogicController(CustomLogicEngine customLogicEngine,
                                BotRuleManager botRuleManager,
                                ResponseTemplateManager responseTemplateManager,
                                ImportExportChangelogRepository changelogRepository,
                                ObjectMapper objectMapper) {
        this.customLogicEngine = customLogicEngine;
        this.botRuleManager = botRuleManager;
        this.responseTemplateManager = responseTemplateManager;
        this.changelogRepository = changelogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Log changelog entry
     */
    private void logChangelog(UUID botId, ImportExportChangelog.OperationType operationType,
                              ImportExportChangelog.SourceType sourceType, String fileName,
                              Integer itemCount, String strategy, ImportExportChangelog.OperationStatus status,
                              Integer importedCount, Integer updatedCount, Integer skippedCount,
                              Integer errorCount, String errorMessage, String performedBy, Long tenantId) {
        try {
            ImportExportChangelog changelog = ImportExportChangelog.builder()
                .botId(botId)
                .operationType(operationType)
                .sourceType(sourceType)
                .fileName(fileName)
                .itemCount(itemCount)
                .strategy(strategy)
                .status(status)
                .importedCount(importedCount)
                .updatedCount(updatedCount)
                .skippedCount(skippedCount)
                .errorCount(errorCount)
                .errorMessage(errorMessage)
                .performedBy(performedBy)
                .tenantId(tenantId)
                .build();
            
            changelogRepository.save(changelog);
            log.info("📝 Changelog logged: {} for botId: {}", operationType, botId);
        } catch (Exception e) {
            log.error("❌ Failed to log changelog: {}", e.getMessage());
            // Don't throw exception - changelog logging should not break main operation
        }
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
            com.chatbot.core.penny.dto.request.MiddlewareRequest mockRequest = 
                com.chatbot.core.penny.dto.request.MiddlewareRequest.builder()
                    .requestId("test-" + System.currentTimeMillis())
                    .userId((String) testData.getOrDefault("userId", "test-user"))
                    .platform((String) testData.getOrDefault("platform", "test"))
                    .message((String) testData.getOrDefault("message", ""))
                    .botId(botId)
                    .tenantKey("test-tenant")
                    .ownerId("test-owner")
                    .language((String) testData.getOrDefault("language", "vi"))
                    .build();
            
            com.chatbot.core.penny.context.ConversationContext mockContext = 
                com.chatbot.core.penny.context.ConversationContext.builder()
                    .contextId("test-context")
                    .botId(botId)
                    .userId((String) testData.getOrDefault("userId", "test-user"))
                    .platform((String) testData.getOrDefault("platform", "test"))
                    .language((String) testData.getOrDefault("language", "vi"))
                    .metadata((Map<String, Object>) testData.getOrDefault("context", Map.of()))
                    .build();
            
            com.chatbot.core.penny.routing.dto.IntentAnalysisResult mockIntent = 
                com.chatbot.core.penny.routing.dto.IntentAnalysisResult.builder()
                    .primaryIntent((String) testData.getOrDefault("intent", "greeting"))
                    .confidence((Double) testData.getOrDefault("confidence", 0.8))
                    .build();
            
            // Process with custom logic
            com.chatbot.core.penny.dto.response.MiddlewareResponse response = 
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
     * Export custom logic configuration with versioning
     */
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportCustomLogic(@PathVariable String botId, Principal principal) {
        UUID botUuid = UUID.fromString(botId);
        Long tenantId = TenantContext.getTenantId();
        
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
                    ruleData.put("createdAt", rule.getCreatedAt().toString());
                    ruleData.put("updatedAt", rule.getUpdatedAt().toString());
                    ruleData.put("executionCount", rule.getExecutionCount());
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
                    templateData.put("createdAt", template.getCreatedAt().toString());
                    templateData.put("updatedAt", template.getUpdatedAt().toString());
                    templateData.put("usageCount", template.getUsageCount());
                    return templateData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> export = new java.util.HashMap<>();
            export.put("version", "1.0");
            export.put("format", "penny-custom-logic");
            export.put("botId", botId);
            export.put("exportedAt", java.time.Instant.now().toString());
            export.put("exportedBy", principal != null ? principal.getName() : "system");
            export.put("rulesCount", exportedRules.size());
            export.put("templatesCount", exportedTemplates.size());
            export.put("rules", exportedRules);
            export.put("templates", exportedTemplates);
            
            // Log changelog
            logChangelog(
                botUuid,
                ImportExportChangelog.OperationType.EXPORT,
                ImportExportChangelog.SourceType.JSON,
                "custom_logic_export.json",
                exportedRules.size() + exportedTemplates.size(),
                null,
                ImportExportChangelog.OperationStatus.SUCCESS,
                0, 0, 0, 0,
                null,
                principal != null ? principal.getName() : "system",
                tenantId
            );
            
            return ResponseEntity.ok(export);
            
        } catch (Exception e) {
            log.error("❌ Error exporting custom logic: {}", e.getMessage(), e);
            
            // Log changelog for failed export
            logChangelog(
                botUuid,
                ImportExportChangelog.OperationType.EXPORT,
                ImportExportChangelog.SourceType.JSON,
                "custom_logic_export.json",
                0,
                null,
                ImportExportChangelog.OperationStatus.FAILED,
                0, 0, 0, 1,
                e.getMessage(),
                principal != null ? principal.getName() : "system",
                tenantId
            );
            
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error exporting custom logic: " + e.getMessage()
            ));
        }
    }

    /**
     * Validate import data before actual import
     */
    @PostMapping("/import/validate")
    public ResponseEntity<Map<String, Object>> validateImport(
            @PathVariable String botId,
            @RequestBody Map<String, Object> importData) {
        
        UUID botUuid = UUID.fromString(botId);
        
        try {
            java.util.List<String> warnings = new java.util.ArrayList<>();
            java.util.List<String> errors = new java.util.ArrayList<>();
            int validRules = 0;
            int validTemplates = 0;
            
            // Validate rules
            if (importData.containsKey("rules")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> rules = 
                    (java.util.List<Map<String, Object>>) importData.get("rules");
                
                for (int i = 0; i < rules.size(); i++) {
                    Map<String, Object> ruleData = rules.get(i);
                    try {
                        // Check required fields
                        if (ruleData.get("name") == null || ((String) ruleData.get("name")).trim().isEmpty()) {
                            errors.add("Rule " + (i + 1) + ": Name is required");
                            continue;
                        }
                        
                        if (ruleData.get("action") == null || ((String) ruleData.get("action")).trim().isEmpty()) {
                            errors.add("Rule " + (i + 1) + ": Action is required");
                            continue;
                        }
                        
                        // Validate enum values
                        if (ruleData.containsKey("ruleType")) {
                            try {
                                BotRule.RuleType.valueOf((String) ruleData.get("ruleType"));
                            } catch (Exception e) {
                                errors.add("Rule " + (i + 1) + ": Invalid ruleType");
                            }
                        }
                        
                        if (ruleData.containsKey("triggerType")) {
                            try {
                                BotRule.TriggerType.valueOf((String) ruleData.get("triggerType"));
                            } catch (Exception e) {
                                errors.add("Rule " + (i + 1) + ": Invalid triggerType");
                            }
                        }
                        
                        validRules++;
                    } catch (Exception e) {
                        errors.add("Rule " + (i + 1) + ": " + e.getMessage());
                    }
                }
            }
            
            // Validate templates
            if (importData.containsKey("templates")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> templates = 
                    (java.util.List<Map<String, Object>>) importData.get("templates");
                
                for (int i = 0; i < templates.size(); i++) {
                    Map<String, Object> templateData = templates.get(i);
                    try {
                        // Check required fields
                        if (templateData.get("name") == null || ((String) templateData.get("name")).trim().isEmpty()) {
                            errors.add("Template " + (i + 1) + ": Name is required");
                            continue;
                        }
                        
                        if (templateData.get("intent") == null || ((String) templateData.get("intent")).trim().isEmpty()) {
                            errors.add("Template " + (i + 1) + ": Intent is required");
                            continue;
                        }
                        
                        if (templateData.get("templateText") == null || ((String) templateData.get("templateText")).trim().isEmpty()) {
                            errors.add("Template " + (i + 1) + ": Template text is required");
                            continue;
                        }
                        
                        // Validate enum values
                        if (templateData.containsKey("templateType")) {
                            try {
                                ResponseTemplate.TemplateType.valueOf((String) templateData.get("templateType"));
                            } catch (Exception e) {
                                errors.add("Template " + (i + 1) + ": Invalid templateType");
                            }
                        }
                        
                        validTemplates++;
                    } catch (Exception e) {
                        errors.add("Template " + (i + 1) + ": " + e.getMessage());
                    }
                }
            }
            
            // Check for potential conflicts
            if (importData.containsKey("rules")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> rules = 
                    (java.util.List<Map<String, Object>>) importData.get("rules");
                
                var existingRules = botRuleManager.getRulesForBot(botUuid);
                java.util.Set<String> existingNames = existingRules.stream()
                    .map(BotRule::getName)
                    .collect(java.util.stream.Collectors.toSet());
                
                for (Map<String, Object> ruleData : rules) {
                    String name = (String) ruleData.get("name");
                    if (existingNames.contains(name)) {
                        warnings.add("Rule '" + name + "' already exists and will be overwritten");
                    }
                }
            }
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("valid", errors.isEmpty());
            response.put("validRules", validRules);
            response.put("validTemplates", validTemplates);
            response.put("warnings", warnings);
            response.put("errors", errors);
            
            if (errors.isEmpty()) {
                response.put("message", "Import data is valid and ready to import");
            } else {
                response.put("message", "Import data has validation errors");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error validating import data: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error validating import data: " + e.getMessage()
            ));
        }
    }

    /**
     * Diff two export versions to see changes
     */
    @PostMapping("/diff")
    public ResponseEntity<Map<String, Object>> diffExports(
            @PathVariable String botId,
            @RequestBody Map<String, Object> diffRequest) {
        
        UUID botUuid = UUID.fromString(botId);
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> version1 = (Map<String, Object>) diffRequest.get("version1");
            @SuppressWarnings("unchecked")
            Map<String, Object> version2 = (Map<String, Object>) diffRequest.get("version2");
            
            if (version1 == null || version2 == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Both version1 and version2 are required"
                ));
            }
            
            java.util.List<String> addedRules = new java.util.ArrayList<>();
            java.util.List<String> removedRules = new java.util.ArrayList<>();
            java.util.List<String> modifiedRules = new java.util.ArrayList<>();
            java.util.List<Map<String, Object>> ruleChanges = new java.util.ArrayList<>();
            
            java.util.List<String> addedTemplates = new java.util.ArrayList<>();
            java.util.List<String> removedTemplates = new java.util.ArrayList<>();
            java.util.List<String> modifiedTemplates = new java.util.ArrayList<>();
            java.util.List<Map<String, Object>> templateChanges = new java.util.ArrayList<>();
            
            // Diff rules
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> rules1 = 
                (java.util.List<Map<String, Object>>) version1.getOrDefault("rules", new java.util.ArrayList<>());
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> rules2 = 
                (java.util.List<Map<String, Object>>) version2.getOrDefault("rules", new java.util.ArrayList<>());
            
            java.util.Map<String, Map<String, Object>> rules1Map = rules1.stream()
                .collect(java.util.stream.Collectors.toMap(
                    r -> (String) r.get("name"),
                    r -> r,
                    (r1, r2) -> r1
                ));
            java.util.Map<String, Map<String, Object>> rules2Map = rules2.stream()
                .collect(java.util.stream.Collectors.toMap(
                    r -> (String) r.get("name"),
                    r -> r,
                    (r1, r2) -> r1
                ));
            
            // Find added rules (in v2 but not in v1)
            for (String ruleName : rules2Map.keySet()) {
                if (!rules1Map.containsKey(ruleName)) {
                    addedRules.add(ruleName);
                }
            }
            
            // Find removed rules (in v1 but not in v2)
            for (String ruleName : rules1Map.keySet()) {
                if (!rules2Map.containsKey(ruleName)) {
                    removedRules.add(ruleName);
                }
            }
            
            // Find modified rules (in both but different)
            for (String ruleName : rules1Map.keySet()) {
                if (rules2Map.containsKey(ruleName)) {
                    Map<String, Object> rule1 = rules1Map.get(ruleName);
                    Map<String, Object> rule2 = rules2Map.get(ruleName);
                    
                    java.util.List<String> changes = new java.util.ArrayList<>();
                    
                    // Compare key fields
                    if (!java.util.Objects.equals(rule1.get("description"), rule2.get("description"))) {
                        changes.add("description");
                    }
                    if (!java.util.Objects.equals(rule1.get("condition"), rule2.get("condition"))) {
                        changes.add("condition");
                    }
                    if (!java.util.Objects.equals(rule1.get("action"), rule2.get("action"))) {
                        changes.add("action");
                    }
                    if (!java.util.Objects.equals(rule1.get("priority"), rule2.get("priority"))) {
                        changes.add("priority");
                    }
                    if (!java.util.Objects.equals(rule1.get("isActive"), rule2.get("isActive"))) {
                        changes.add("isActive");
                    }
                    
                    if (!changes.isEmpty()) {
                        modifiedRules.add(ruleName);
                        Map<String, Object> changeDetail = new java.util.HashMap<>();
                        changeDetail.put("name", ruleName);
                        changeDetail.put("changedFields", changes);
                        ruleChanges.add(changeDetail);
                    }
                }
            }
            
            // Diff templates (similar logic)
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> templates1 = 
                (java.util.List<Map<String, Object>>) version1.getOrDefault("templates", new java.util.ArrayList<>());
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> templates2 = 
                (java.util.List<Map<String, Object>>) version2.getOrDefault("templates", new java.util.ArrayList<>());
            
            java.util.Map<String, Map<String, Object>> templates1Map = templates1.stream()
                .collect(java.util.stream.Collectors.toMap(
                    t -> (String) t.get("name"),
                    t -> t,
                    (t1, t2) -> t1
                ));
            java.util.Map<String, Map<String, Object>> templates2Map = templates2.stream()
                .collect(java.util.stream.Collectors.toMap(
                    t -> (String) t.get("name"),
                    t -> t,
                    (t1, t2) -> t1
                ));
            
            for (String templateName : templates2Map.keySet()) {
                if (!templates1Map.containsKey(templateName)) {
                    addedTemplates.add(templateName);
                }
            }
            
            for (String templateName : templates1Map.keySet()) {
                if (!templates2Map.containsKey(templateName)) {
                    removedTemplates.add(templateName);
                }
            }
            
            for (String templateName : templates1Map.keySet()) {
                if (templates2Map.containsKey(templateName)) {
                    Map<String, Object> template1 = templates1Map.get(templateName);
                    Map<String, Object> template2 = templates2Map.get(templateName);
                    
                    java.util.List<String> changes = new java.util.ArrayList<>();
                    
                    if (!java.util.Objects.equals(template1.get("description"), template2.get("description"))) {
                        changes.add("description");
                    }
                    if (!java.util.Objects.equals(template1.get("intent"), template2.get("intent"))) {
                        changes.add("intent");
                    }
                    if (!java.util.Objects.equals(template1.get("templateText"), template2.get("templateText"))) {
                        changes.add("templateText");
                    }
                    if (!java.util.Objects.equals(template1.get("priority"), template2.get("priority"))) {
                        changes.add("priority");
                    }
                    if (!java.util.Objects.equals(template1.get("isActive"), template2.get("isActive"))) {
                        changes.add("isActive");
                    }
                    
                    if (!changes.isEmpty()) {
                        modifiedTemplates.add(templateName);
                        Map<String, Object> changeDetail = new java.util.HashMap<>();
                        changeDetail.put("name", templateName);
                        changeDetail.put("changedFields", changes);
                        templateChanges.add(changeDetail);
                    }
                }
            }
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Diff completed successfully");
            response.put("version1Info", Map.of(
                "exportedAt", version1.get("exportedAt"),
                "rulesCount", rules1.size(),
                "templatesCount", templates1.size()
            ));
            response.put("version2Info", Map.of(
                "exportedAt", version2.get("exportedAt"),
                "rulesCount", rules2.size(),
                "templatesCount", templates2.size()
            ));
            response.put("rules", Map.of(
                "added", addedRules,
                "removed", removedRules,
                "modified", modifiedRules,
                "changes", ruleChanges
            ));
            response.put("templates", Map.of(
                "added", addedTemplates,
                "removed", removedTemplates,
                "modified", modifiedTemplates,
                "changes", templateChanges
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error diffing exports: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error diffing exports: " + e.getMessage()
            ));
        }
    }

    /**
     * Import custom logic configuration with merge strategy
     */
    @PostMapping("/import")
    @Transactional(rollbackFor = Exception.class)
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
        
        // Check for dry-run mode
        boolean dryRun = Boolean.TRUE.equals(importData.get("dryRun"));
        
        // Get import strategy (default: MERGE)
        ImportStrategy strategy = ImportStrategy.MERGE;
        if (importData.containsKey("strategy")) {
            try {
                strategy = ImportStrategy.valueOf(((String) importData.get("strategy")).toUpperCase());
            } catch (Exception e) {
                log.warn("⚠️ Invalid import strategy, defaulting to MERGE");
            }
        }
        
        try {
            int importedRules = 0;
            int updatedRules = 0;
            int skippedRules = 0;
            int importedTemplates = 0;
            int updatedTemplates = 0;
            int skippedTemplates = 0;
            java.util.List<String> errors = new java.util.ArrayList<>();
            java.util.List<Map<String, String>> previewChanges = new java.util.ArrayList<>();
            
            // Get existing rules for conflict checking
            var existingRules = botRuleManager.getRulesForBot(botUuid);
            java.util.Map<String, BotRule> existingRuleMap = existingRules.stream()
                .collect(java.util.stream.Collectors.toMap(BotRule::getName, r -> r, (r1, r2) -> r1));
            
            // Get existing templates for conflict checking
            var existingTemplates = responseTemplateManager.getTemplatesForBot(botUuid);
            java.util.Map<String, ResponseTemplate> existingTemplateMap = existingTemplates.stream()
                .collect(java.util.stream.Collectors.toMap(ResponseTemplate::getName, t -> t, (t1, t2) -> t1));
            
            // Import rules
            if (importData.containsKey("rules")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> rules = 
                    (java.util.List<Map<String, Object>>) importData.get("rules");
                
                for (Map<String, Object> ruleData : rules) {
                    String ruleName = (String) ruleData.get("name");
                    try {
                        BotRule existingRule = existingRuleMap.get(ruleName);
                        
                        if (existingRule != null) {
                            // Rule already exists
                            switch (strategy) {
                                case OVERWRITE:
                                    if (!dryRun) {
                                        botRuleManager.deleteRule(existingRule.getId(), createdBy);
                                        botRuleManager.createRule(
                                            botUuid,
                                            ruleName,
                                            (String) ruleData.get("description"),
                                            (String) ruleData.get("condition"),
                                            (String) ruleData.get("action"),
                                            BotRule.RuleType.valueOf((String) ruleData.get("ruleType")),
                                            BotRule.TriggerType.valueOf((String) ruleData.get("triggerType")),
                                            (String) ruleData.get("triggerValue"),
                                            (Integer) ruleData.get("priority"),
                                            createdBy
                                        );
                                    }
                                    importedRules++;
                                    previewChanges.add(Map.of(
                                        "type", "rule",
                                        "name", ruleName,
                                        "action", "overwrite",
                                        "description", "Delete existing and create new"
                                    ));
                                    break;
                                case SKIP:
                                case APPEND:
                                    skippedRules++;
                                    previewChanges.add(Map.of(
                                        "type", "rule",
                                        "name", ruleName,
                                        "action", "skip",
                                        "description", "Skip existing rule"
                                    ));
                                    break;
                                case MERGE:
                                default:
                                    if (!dryRun) {
                                        botRuleManager.updateRule(
                                            existingRule.getId(),
                                            ruleName,
                                            (String) ruleData.get("description"),
                                            (String) ruleData.get("condition"),
                                            (String) ruleData.get("action"),
                                            (Integer) ruleData.get("priority"),
                                            (Boolean) ruleData.getOrDefault("isActive", true),
                                            createdBy
                                        );
                                    }
                                    updatedRules++;
                                    previewChanges.add(Map.of(
                                        "type", "rule",
                                        "name", ruleName,
                                        "action", "update",
                                        "description", "Update existing rule"
                                    ));
                                    break;
                            }
                        } else {
                            // New rule, always import
                            if (!dryRun) {
                                botRuleManager.createRule(
                                    botUuid,
                                    ruleName,
                                    (String) ruleData.get("description"),
                                    (String) ruleData.get("condition"),
                                    (String) ruleData.get("action"),
                                    BotRule.RuleType.valueOf((String) ruleData.get("ruleType")),
                                    BotRule.TriggerType.valueOf((String) ruleData.get("triggerType")),
                                    (String) ruleData.get("triggerValue"),
                                    (Integer) ruleData.get("priority"),
                                    createdBy
                                );
                            }
                            importedRules++;
                            previewChanges.add(Map.of(
                                "type", "rule",
                                "name", ruleName,
                                "action", "create",
                                "description", "Create new rule"
                            ));
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Failed to import rule: {}", e.getMessage());
                        errors.add("Rule '" + ruleName + "': " + e.getMessage());
                    }
                }
            }
            
            // Import templates
            if (importData.containsKey("templates")) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> templates = 
                    (java.util.List<Map<String, Object>>) importData.get("templates");
                
                for (Map<String, Object> templateData : templates) {
                    String templateName = (String) templateData.get("name");
                    try {
                        ResponseTemplate existingTemplate = existingTemplateMap.get(templateName);
                        
                        if (existingTemplate != null) {
                            // Template already exists
                            switch (strategy) {
                                case OVERWRITE:
                                    if (!dryRun) {
                                        responseTemplateManager.deleteTemplate(existingTemplate.getId(), createdBy);
                                        responseTemplateManager.createTemplate(
                                            botUuid,
                                            templateName,
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
                                    }
                                    importedTemplates++;
                                    previewChanges.add(Map.of(
                                        "type", "template",
                                        "name", templateName,
                                        "action", "overwrite",
                                        "description", "Delete existing and create new"
                                    ));
                                    break;
                                case SKIP:
                                case APPEND:
                                    skippedTemplates++;
                                    previewChanges.add(Map.of(
                                        "type", "template",
                                        "name", templateName,
                                        "action", "skip",
                                        "description", "Skip existing template"
                                    ));
                                    break;
                                case MERGE:
                                default:
                                    if (!dryRun) {
                                        Object variablesObj = templateData.get("variables");
                                        Map<String, Object> variables = variablesObj instanceof Map ? (Map<String, Object>) variablesObj : null;
                                        
                                        Object quickRepliesObj = templateData.get("quickReplies");
                                        java.util.List<Map<String, Object>> quickReplies = quickRepliesObj instanceof List ? (java.util.List<Map<String, Object>>) quickRepliesObj : null;
                                        
                                        Object isActiveObj = templateData.getOrDefault("isActive", true);
                                        Boolean isActive = isActiveObj != null ? Boolean.valueOf(isActiveObj.toString()) : true;
                                        
                                        responseTemplateManager.updateTemplate(
                                            existingTemplate.getId(),
                                            templateName,
                                            (String) templateData.get("description"),
                                            (String) templateData.get("intent"),
                                            (String) templateData.get("templateText"),
                                            ResponseTemplate.TemplateType.valueOf((String) templateData.get("templateType")),
                                            (String) templateData.get("language"),
                                            (Integer) templateData.get("priority"),
                                            isActive,
                                            variables,
                                            quickReplies,
                                            createdBy
                                        );
                                    }
                                    updatedTemplates++;
                                    previewChanges.add(Map.of(
                                        "type", "template",
                                        "name", templateName,
                                        "action", "update",
                                        "description", "Update existing template"
                                    ));
                                    break;
                            }
                        } else {
                            // New template, always import
                            if (!dryRun) {
                                responseTemplateManager.createTemplate(
                                    botUuid,
                                    templateName,
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
                            }
                            importedTemplates++;
                            previewChanges.add(Map.of(
                                "type", "template",
                                "name", templateName,
                                "action", "create",
                                "description", "Create new template"
                            ));
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Failed to import template: {}", e.getMessage());
                        errors.add("Template '" + templateName + "': " + e.getMessage());
                    }
                }
            }
            
            Map<String, Object> response = new java.util.HashMap<>();
            
            if (dryRun) {
                response.put("message", "Dry-run completed - no changes were made");
                response.put("dryRun", true);
                
                // Log changelog for dry-run
                logChangelog(
                    botUuid,
                    ImportExportChangelog.OperationType.IMPORT,
                    ImportExportChangelog.SourceType.JSON,
                    "custom_logic_import.json",
                    importedRules + updatedRules + skippedRules + importedTemplates + updatedTemplates + skippedTemplates,
                    strategy.name(),
                    ImportExportChangelog.OperationStatus.DRY_RUN,
                    importedRules, updatedRules, skippedRules,
                    errors.size(),
                    errors.isEmpty() ? null : String.join("; ", errors),
                    createdBy,
                    tenantId
                );
            } else {
                response.put("message", "Custom logic imported successfully");
                response.put("dryRun", false);
                
                // Log changelog for actual import
                ImportExportChangelog.OperationStatus status = errors.isEmpty() 
                    ? ImportExportChangelog.OperationStatus.SUCCESS 
                    : ImportExportChangelog.OperationStatus.PARTIAL;
                
                logChangelog(
                    botUuid,
                    ImportExportChangelog.OperationType.IMPORT,
                    ImportExportChangelog.SourceType.JSON,
                    "custom_logic_import.json",
                    importedRules + updatedRules + skippedRules + importedTemplates + updatedTemplates + skippedTemplates,
                    strategy.name(),
                    status,
                    importedRules, updatedRules, skippedRules,
                    errors.size(),
                    errors.isEmpty() ? null : String.join("; ", errors),
                    createdBy,
                    tenantId
                );
            }
            
            response.put("strategy", strategy.name());
            response.put("importedRules", importedRules);
            response.put("updatedRules", updatedRules);
            response.put("skippedRules", skippedRules);
            response.put("importedTemplates", importedTemplates);
            response.put("updatedTemplates", updatedTemplates);
            response.put("skippedTemplates", skippedTemplates);
            response.put("botId", botId);
            response.put("previewChanges", previewChanges);
            
            if (!errors.isEmpty()) {
                response.put("errors", errors);
                response.put("errorCount", errors.size());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error importing custom logic: {}", e.getMessage(), e);
            
            // Log changelog for failed import
            logChangelog(
                botUuid,
                ImportExportChangelog.OperationType.IMPORT,
                ImportExportChangelog.SourceType.JSON,
                "custom_logic_import.json",
                0,
                strategy != null ? strategy.name() : "MERGE",
                ImportExportChangelog.OperationStatus.FAILED,
                0, 0, 0, 1,
                e.getMessage(),
                createdBy,
                tenantId
            );
            
            // Transaction will automatically rollback due to @Transactional
            throw new RuntimeException("Import failed, transaction rolled back: " + e.getMessage(), e);
        }
    }

    /**
     * Import from Botpress JSON format
     */
    @PostMapping("/import/botpress")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Map<String, Object>> importFromBotpress(
            @PathVariable String botId,
            @RequestBody BotpressImportDto botpressData,
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
            int importedIntents = 0;
            java.util.List<String> errors = new java.util.ArrayList<>();
            
            // Convert Botpress QnA to Penny rules
            if (botpressData.getQna() != null && botpressData.getQna().getItems() != null) {
                for (BotpressImportDto.QnAItem qnaItem : botpressData.getQna().getItems()) {
                    try {
                        if (qnaItem.getQuestions() != null && !qnaItem.getQuestions().isEmpty()) {
                            String triggerValue = qnaItem.getQuestions().get(0);
                            
                            // Build action from answers
                            StringBuilder actionBuilder = new StringBuilder();
                            if (qnaItem.getAnswers() != null && !qnaItem.getAnswers().isEmpty()) {
                                for (BotpressImportDto.QnAAnswer answer : qnaItem.getAnswers()) {
                                    if (answer.getText() != null) {
                                        actionBuilder.append(answer.getText()).append("\n");
                                    }
                                }
                            }
                            
                            // Create rule
                            botRuleManager.createRule(
                                botUuid,
                                "Botpress QnA: " + (qnaItem.getId() != null ? qnaItem.getId() : "Imported"),
                                "Imported from Botpress QnA",
                                null,
                                actionBuilder.toString().trim(),
                                BotRule.RuleType.RESPONSE,
                                BotRule.TriggerType.INTENT,
                                triggerValue,
                                0,
                                createdBy
                            );
                            importedRules++;
                        }
                    } catch (Exception e) {
                        String errorMsg = "Failed to import QnA item " + qnaItem.getId() + ": " + e.getMessage();
                        log.warn("⚠️ {}", errorMsg);
                        errors.add(errorMsg);
                    }
                }
            }
            
            // Convert Botpress Intents to Penny rules (as keyword triggers)
            if (botpressData.getIntents() != null && botpressData.getIntents().getItems() != null) {
                for (BotpressImportDto.IntentItem intentItem : botpressData.getIntents().getItems()) {
                    try {
                        if (intentItem.getUtterances() != null && !intentItem.getUtterances().isEmpty()) {
                            // Use intent name as trigger
                            String triggerValue = intentItem.getName();
                            
                            // Build action from utterances (sample responses)
                            StringBuilder actionBuilder = new StringBuilder();
                            actionBuilder.append("Intent: ").append(intentItem.getName()).append("\n");
                            actionBuilder.append("Sample utterances:\n");
                            for (BotpressImportDto.Utterance utterance : intentItem.getUtterances()) {
                                if (utterance.getText() != null) {
                                    actionBuilder.append("- ").append(utterance.getText()).append("\n");
                                }
                            }
                            
                            // Create rule with KEYWORD trigger type
                            botRuleManager.createRule(
                                botUuid,
                                "Botpress Intent: " + intentItem.getName(),
                                "Imported from Botpress Intent",
                                null,
                                actionBuilder.toString().trim(),
                                BotRule.RuleType.RESPONSE,
                                BotRule.TriggerType.KEYWORD,
                                triggerValue,
                                0,
                                createdBy
                            );
                            importedIntents++;
                        }
                    } catch (Exception e) {
                        String errorMsg = "Failed to import Intent item " + intentItem.getId() + ": " + e.getMessage();
                        log.warn("⚠️ {}", errorMsg);
                        errors.add(errorMsg);
                    }
                }
            }
            
            // Convert Botpress Flows to Penny rules (basic conversion)
            if (botpressData.getFlows() != null && botpressData.getFlows().getItems() != null) {
                for (BotpressImportDto.FlowItem flowItem : botpressData.getFlows().getItems()) {
                    try {
                        if (flowItem.getNodes() != null && !flowItem.getNodes().isEmpty()) {
                            // For each flow, create a rule that represents the flow entry point
                            String flowName = flowItem.getName();
                            
                            // Build action from flow nodes
                            StringBuilder actionBuilder = new StringBuilder();
                            actionBuilder.append("Flow: ").append(flowName).append("\n");
                            actionBuilder.append("Nodes:\n");
                            for (BotpressImportDto.FlowNode node : flowItem.getNodes()) {
                                if (node.getName() != null) {
                                    actionBuilder.append("- ").append(node.getName()).append(" (").append(node.getType()).append(")\n");
                                }
                            }
                            
                            // Create rule with CONDITION trigger type
                            botRuleManager.createRule(
                                botUuid,
                                "Botpress Flow: " + flowName,
                                "Imported from Botpress Flow",
                                "flow_name == '" + flowName + "'",
                                actionBuilder.toString().trim(),
                                BotRule.RuleType.RESPONSE,
                                BotRule.TriggerType.CONDITION,
                                flowName,
                                0,
                                createdBy
                            );
                            importedRules++;
                        }
                    } catch (Exception e) {
                        String errorMsg = "Failed to import Flow item " + flowItem.getId() + ": " + e.getMessage();
                        log.warn("⚠️ {}", errorMsg);
                        errors.add(errorMsg);
                    }
                }
            }
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Botpress JSON imported successfully");
            response.put("importedRules", importedRules);
            response.put("importedTemplates", importedTemplates);
            response.put("importedIntents", importedIntents);
            response.put("botId", botId);
            
            if (!errors.isEmpty()) {
                response.put("errors", errors);
                response.put("errorCount", errors.size());
            }
            
            // Log changelog
            ImportExportChangelog.OperationStatus status = errors.isEmpty() 
                ? ImportExportChangelog.OperationStatus.SUCCESS 
                : ImportExportChangelog.OperationStatus.PARTIAL;
            
            logChangelog(
                botUuid,
                ImportExportChangelog.OperationType.IMPORT,
                ImportExportChangelog.SourceType.BOTPRESS,
                "botpress_import.json",
                importedRules + importedTemplates + importedIntents,
                null,
                status,
                importedRules, 0, 0,
                errors.size(),
                errors.isEmpty() ? null : String.join("; ", errors),
                createdBy,
                tenantId
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error importing from Botpress: {}", e.getMessage(), e);
            
            // Log changelog for failed import
            logChangelog(
                botUuid,
                ImportExportChangelog.OperationType.IMPORT,
                ImportExportChangelog.SourceType.BOTPRESS,
                "botpress_import.json",
                0,
                null,
                ImportExportChangelog.OperationStatus.FAILED,
                0, 0, 0, 1,
                e.getMessage(),
                createdBy,
                tenantId
            );
            
            // Transaction will automatically rollback due to @Transactional
            throw new RuntimeException("Botpress import failed, transaction rolled back: " + e.getMessage(), e);
        }
    }

    /**
     * Export templates to CSV file
     * CSV format: name,description,intent,templateText,templateType,language,priority,isActive
     */
    @GetMapping("/export/templates/csv")
    public ResponseEntity<byte[]> exportTemplatesToCSV(@PathVariable String botId) {
        UUID botUuid = UUID.fromString(botId);
        
        try {
            List<ResponseTemplate> templates = responseTemplateManager.getTemplatesForBot(botUuid);
            
            // Build CSV content
            StringBuilder csvBuilder = new StringBuilder();
            
            // Header
            csvBuilder.append("name,description,intent,templateText,templateType,language,priority,isActive\n");
            
            // Data rows
            for (ResponseTemplate template : templates) {
                csvBuilder.append(escapeCSV(template.getName())).append(",");
                csvBuilder.append(escapeCSV(template.getDescription())).append(",");
                csvBuilder.append(escapeCSV(template.getIntent())).append(",");
                csvBuilder.append(escapeCSV(template.getTemplateText())).append(",");
                csvBuilder.append(template.getTemplateType().name()).append(",");
                csvBuilder.append(escapeCSV(template.getLanguage())).append(",");
                csvBuilder.append(template.getPriority()).append(",");
                csvBuilder.append(template.getIsActive()).append("\n");
            }
            
            byte[] csvBytes = csvBuilder.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            
            return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=\"templates_" + botId + ".csv\"")
                .body(csvBytes);
            
        } catch (Exception e) {
            log.error("❌ Error exporting templates to CSV: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Escape CSV value (handle quotes, commas, newlines)
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    /**
     * Get changelog history for a bot
     */
    @GetMapping("/changelog")
    public ResponseEntity<Map<String, Object>> getChangelog(@PathVariable String botId) {
        UUID botUuid = UUID.fromString(botId);
        Long tenantId = TenantContext.getTenantId();
        
        try {
            List<ImportExportChangelog> changelogs = changelogRepository.findByBotIdOrderByPerformedAtDesc(botUuid);
            
            // Filter by tenant for security
            List<Map<String, Object>> filteredChangelogs = changelogs.stream()
                .filter(c -> c.getTenantId() == null || c.getTenantId().equals(tenantId))
                .map(c -> {
                    Map<String, Object> changelogData = new java.util.HashMap<>();
                    changelogData.put("id", c.getId());
                    changelogData.put("operationType", c.getOperationType());
                    changelogData.put("sourceType", c.getSourceType());
                    changelogData.put("fileName", c.getFileName());
                    changelogData.put("fileSize", c.getFileSize());
                    changelogData.put("itemCount", c.getItemCount());
                    changelogData.put("strategy", c.getStrategy());
                    changelogData.put("status", c.getStatus());
                    changelogData.put("importedCount", c.getImportedCount());
                    changelogData.put("updatedCount", c.getUpdatedCount());
                    changelogData.put("skippedCount", c.getSkippedCount());
                    changelogData.put("errorCount", c.getErrorCount());
                    changelogData.put("errorMessage", c.getErrorMessage());
                    changelogData.put("performedBy", c.getPerformedBy());
                    changelogData.put("performedAt", c.getPerformedAt());
                    return changelogData;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Changelog retrieved successfully");
            response.put("botId", botId);
            response.put("count", filteredChangelogs.size());
            response.put("changelogs", filteredChangelogs);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error retrieving changelog: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error retrieving changelog: " + e.getMessage()
            ));
        }
    }
}
