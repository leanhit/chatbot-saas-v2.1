package com.chatbot.shared.penny.rules;

import com.chatbot.core.tenant.infra.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Bot Rule Controller - API quản lý rules tùy chỉnh cho bot
 */
@RestController
@RequestMapping("/api/penny/bots/{botId}/rules")
@Slf4j
public class BotRuleController {

    private final BotRuleManager botRuleManager;

    public BotRuleController(BotRuleManager botRuleManager) {
        this.botRuleManager = botRuleManager;
    }

    /**
     * Create new rule
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRule(
            @PathVariable String botId,
            @RequestBody Map<String, Object> request,
            Principal principal) {
        
        // Validate tenant context
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Tenant context not found. Please provide X-Tenant-Key header"
            ));
        }
        
        String createdBy = principal.getName();
        UUID botUuid = UUID.fromString(botId);
        
        try {
            BotRule rule = botRuleManager.createRule(
                botUuid,
                (String) request.get("name"),
                (String) request.get("description"),
                (String) request.get("condition"),
                (String) request.get("action"),
                BotRule.RuleType.valueOf((String) request.getOrDefault("ruleType", "RESPONSE")),
                BotRule.TriggerType.valueOf((String) request.getOrDefault("triggerType", "INTENT")),
                (String) request.get("triggerValue"),
                (Integer) request.get("priority"),
                createdBy
            );
            
            return ResponseEntity.ok(Map.of(
                "ruleId", rule.getId().toString(),
                "name", rule.getName(),
                "ruleType", rule.getRuleType().name(),
                "triggerType", rule.getTriggerType().name(),
                "triggerValue", rule.getTriggerValue(),
                "priority", rule.getPriority(),
                "isActive", rule.getIsActive(),
                "message", "Rule created successfully"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ Error creating rule: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Internal server error"
            ));
        }
    }

    /**
     * Update existing rule
     */
    @PutMapping("/{ruleId}")
    public ResponseEntity<Map<String, Object>> updateRule(
            @PathVariable String botId,
            @PathVariable String ruleId,
            @RequestBody Map<String, Object> request,
            Principal principal) {
        
        String updatedBy = principal.getName();
        UUID ruleUuid = UUID.fromString(ruleId);
        
        try {
            BotRule rule = botRuleManager.updateRule(
                ruleUuid,
                (String) request.get("name"),
                (String) request.get("description"),
                (String) request.get("condition"),
                (String) request.get("action"),
                (Integer) request.get("priority"),
                (Boolean) request.get("isActive"),
                updatedBy
            );
            
            return ResponseEntity.ok(Map.of(
                "ruleId", rule.getId().toString(),
                "name", rule.getName(),
                "ruleType", rule.getRuleType().name(),
                "triggerType", rule.getTriggerType().name(),
                "triggerValue", rule.getTriggerValue(),
                "priority", rule.getPriority(),
                "isActive", rule.getIsActive(),
                "message", "Rule updated successfully"
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("❌ Error updating rule: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Internal server error"
            ));
        }
    }

    /**
     * Get all rules for bot
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getRules(@PathVariable String botId) {
        UUID botUuid = UUID.fromString(botId);
        
        List<BotRule> rules = botRuleManager.getRulesForBot(botUuid);
        
        List<Map<String, Object>> response = rules.stream()
            .map(rule -> {
                Map<String, Object> ruleMap = new java.util.HashMap<>();
                ruleMap.put("ruleId", rule.getId().toString());
                ruleMap.put("name", rule.getName());
                ruleMap.put("description", rule.getDescription());
                ruleMap.put("ruleType", rule.getRuleType().name());
                ruleMap.put("triggerType", rule.getTriggerType().name());
                ruleMap.put("triggerValue", rule.getTriggerValue());
                ruleMap.put("priority", rule.getPriority());
                ruleMap.put("isActive", rule.getIsActive());
                ruleMap.put("executionCount", rule.getExecutionCount());
                ruleMap.put("createdAt", rule.getCreatedAt().toString());
                ruleMap.put("updatedAt", rule.getUpdatedAt().toString());
                return ruleMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get rules by trigger type
     */
    @GetMapping("/by-type/{triggerType}")
    public ResponseEntity<List<Map<String, Object>>> getRulesByType(
            @PathVariable String botId,
            @PathVariable String triggerType) {
        
        UUID botUuid = UUID.fromString(botId);
        BotRule.TriggerType type = BotRule.TriggerType.valueOf(triggerType.toUpperCase());
        
        List<BotRule> rules = botRuleManager.getRulesByType(botUuid, type);
        
        List<Map<String, Object>> response = rules.stream()
            .map(rule -> {
                Map<String, Object> ruleMap = new java.util.HashMap<>();
                ruleMap.put("ruleId", rule.getId().toString());
                ruleMap.put("name", rule.getName());
                ruleMap.put("description", rule.getDescription());
                ruleMap.put("triggerValue", rule.getTriggerValue());
                ruleMap.put("priority", rule.getPriority());
                ruleMap.put("isActive", rule.getIsActive());
                ruleMap.put("executionCount", rule.getExecutionCount());
                return ruleMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete rule
     */
    @DeleteMapping("/{ruleId}")
    public ResponseEntity<Map<String, String>> deleteRule(
            @PathVariable String botId,
            @PathVariable String ruleId,
            Principal principal) {
        
        String deletedBy = principal.getName();
        UUID ruleUuid = UUID.fromString(ruleId);
        
        boolean success = botRuleManager.deleteRule(ruleUuid, deletedBy);
        
        if (success) {
            return ResponseEntity.ok(Map.of(
                "message", "Rule deleted successfully",
                "ruleId", ruleId
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to delete rule",
                "ruleId", ruleId
            ));
        }
    }

    /**
     * Test rule with sample data
     */
    @PostMapping("/{ruleId}/test")
    public ResponseEntity<Map<String, Object>> testRule(
            @PathVariable String botId,
            @PathVariable String ruleId,
            @RequestBody Map<String, Object> testData) {
        
        UUID ruleUuid = UUID.fromString(ruleId);
        
        try {
            // Get rule
            List<BotRule> rules = botRuleManager.getRulesForBot(UUID.fromString(botId));
            BotRule rule = rules.stream()
                .filter(r -> r.getId().equals(ruleUuid))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rule not found"));
            
            // Create mock context
            com.chatbot.shared.penny.context.ConversationContext mockContext = 
                com.chatbot.shared.penny.context.ConversationContext.builder()
                    .contextId("test")
                    .botId(botId)
                    .userId("test-user")
                    .platform("test")
                    .metadata((Map<String, Object>) testData.getOrDefault("context", Map.of()))
                    .build();
            
            // Test rule matching
            boolean matches = rule.matchesTrigger(
                (String) testData.get("intent"),
                (String) testData.get("message"),
                mockContext.getMetadata()
            );
            
            Map<String, Object> result = Map.of(
                "ruleId", ruleId,
                "ruleName", rule.getName(),
                "matches", matches,
                "testData", testData
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ Error testing rule: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error testing rule: " + e.getMessage()
            ));
        }
    }

    /**
     * Get rule statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRuleStatistics(@PathVariable String botId) {
        UUID botUuid = UUID.fromString(botId);
        
        List<BotRule> rules = botRuleManager.getRulesForBot(botUuid);
        
        long totalRules = rules.size();
        long activeRules = rules.stream().mapToLong(r -> r.getIsActive() ? 1 : 0).sum();
        long totalExecutions = rules.stream().mapToLong(BotRule::getExecutionCount).sum();
        
        Map<String, Long> rulesByType = rules.stream()
            .collect(Collectors.groupingBy(
                r -> r.getRuleType().name(),
                Collectors.counting()
            ));
        
        Map<String, Long> rulesByTriggerType = rules.stream()
            .collect(Collectors.groupingBy(
                r -> r.getTriggerType().name(),
                Collectors.counting()
            ));
        
        return ResponseEntity.ok(Map.of(
            "totalRules", totalRules,
            "activeRules", activeRules,
            "totalExecutions", totalExecutions,
            "rulesByType", rulesByType,
            "rulesByTriggerType", rulesByTriggerType
        ));
    }
}
