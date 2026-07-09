package com.chatbot.shared.penny.rules;

import com.chatbot.shared.penny.context.ConversationContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bot Rule Manager - Quản lý rules tùy chỉnh cho bot
 */
@Service
@Slf4j
public class BotRuleManager {

    private final BotRuleRepository botRuleRepository;
    private final ResponseTemplateRepository responseTemplateRepository;
    private final ObjectMapper objectMapper;

    public BotRuleManager(BotRuleRepository botRuleRepository,
                         ResponseTemplateRepository responseTemplateRepository,
                         ObjectMapper objectMapper) {
        this.botRuleRepository = botRuleRepository;
        this.responseTemplateRepository = responseTemplateRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Create new rule
     */
    @Transactional
    public BotRule createRule(UUID botId, String name, String description, String condition,
                             String action, BotRule.RuleType ruleType, BotRule.TriggerType triggerType,
                             String triggerValue, Integer priority, String createdBy) {
        
        log.info("📝 Creating new rule: {} for bot: {}", name, botId);
        
        // Check if rule name already exists
        if (botRuleRepository.existsByBotIdAndNameAndIsActiveTrue(botId, name)) {
            throw new IllegalArgumentException("Rule with name '" + name + "' already exists for this bot");
        }
        
        BotRule rule = BotRule.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name(name)
                .description(description)
                .condition(condition)
                .action(action)
                .ruleType(ruleType)
                .triggerType(triggerType)
                .triggerValue(triggerValue)
                .priority(priority != null ? priority : 0)
                .isActive(true)
                .createdBy(createdBy)
                .build();
        
        BotRule savedRule = botRuleRepository.save(rule);
        log.info("✅ Rule created successfully: {} (ID: {})", savedRule.getName(), savedRule.getId());
        
        return savedRule;
    }

    /**
     * Update existing rule
     */
    @Transactional
    public BotRule updateRule(UUID ruleId, String name, String description, String condition,
                             String action, Integer priority, Boolean isActive, String updatedBy) {
        
        log.info("🔄 Updating rule: {}", ruleId);
        
        BotRule rule = botRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        
        // Update fields
        if (name != null && !name.trim().isEmpty()) {
            rule.setName(name);
        }
        if (description != null) {
            rule.setDescription(description);
        }
        if (condition != null) {
            rule.setCondition(condition);
        }
        if (action != null) {
            rule.setAction(action);
        }
        if (priority != null) {
            rule.setPriority(priority);
        }
        if (isActive != null) {
            rule.setIsActive(isActive);
        }
        
        rule.setUpdatedBy(updatedBy);
        
        BotRule updatedRule = botRuleRepository.save(rule);
        log.info("✅ Rule updated successfully: {}", updatedRule.getId());
        
        return updatedRule;
    }

    /**
     * Delete rule (hard delete)
     */
    @Transactional
    public boolean deleteRule(UUID ruleId, String deletedBy) {
        log.info("🗑️ Deleting rule: {} by: {}", ruleId, deletedBy);

        try {
            botRuleRepository.deleteById(ruleId);
            log.info("✅ Rule deleted successfully: {}", ruleId);
            return true;
        } catch (Exception e) {
            log.error("❌ Error deleting rule {}: {}", ruleId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get rules for bot (for UI display - includes inactive rules)
     */
    public List<BotRule> getAllRulesForBot(UUID botId) {
        return botRuleRepository.findByBotIdOrderByPriorityDesc(botId);
    }

    /**
     * Get active rules for bot (for rule evaluation)
     */
    public List<BotRule> getRulesForBot(UUID botId) {
        return botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId);
    }

    /**
     * Get rules by type
     */
    public List<BotRule> getRulesByType(UUID botId, BotRule.TriggerType triggerType) {
        return botRuleRepository.findByBotIdAndTriggerTypeAndIsActiveTrueOrderByPriorityDesc(botId, triggerType);
    }

    /**
     * Evaluate rules for current context
     */
    public RuleEvaluationResult evaluateRules(UUID botId, String intent, String message, 
                                              ConversationContext context) {
        log.debug("🔍 Evaluating rules for bot: {}, intent: {}", botId, intent);
        
        List<BotRule> rules = getRulesForBot(botId);
        
        // Find matching rule
        for (BotRule rule : rules) {
            if (rule.matchesTrigger(intent, message, context.getMetadata())) {
                log.info("🎯 Rule matched: {} (ID: {})", rule.getName(), rule.getId());
                
                // Increment execution count
                rule.incrementExecutionCount();
                botRuleRepository.save(rule);
                
                // Process rule action
                String response = processRuleAction(rule, context);
                
                return RuleEvaluationResult.builder()
                        .matched(true)
                        .rule(rule)
                        .response(response)
                        .ruleType(rule.getRuleType())
                        .build();
            }
        }
        
        log.debug("❌ No rules matched for intent: {}", intent);
        return RuleEvaluationResult.builder()
                .matched(false)
                .build();
    }

    /**
     * Process rule action based on rule type
     */
    private String processRuleAction(BotRule rule, ConversationContext context) {
        try {
            switch (rule.getRuleType()) {
                case RESPONSE:
                    return processResponseAction(rule, context);
                case REDIRECT:
                    return processRedirectAction(rule, context);
                case WEBHOOK:
                    return processWebhookAction(rule, context);
                case SCRIPT:
                    return processScriptAction(rule, context);
                default:
                    return "Xin lỗi, tôi không hiểu yêu cầu của bạn.";
            }
        } catch (Exception e) {
            log.error("❌ Error processing rule action for rule {}: {}", rule.getId(), e.getMessage(), e);
            return "Xin lỗi, có lỗi xảy ra khi xử lý yêu cầu của bạn.";
        }
    }

    /**
     * Process response action
     */
    private String processResponseAction(BotRule rule, ConversationContext context) throws JsonProcessingException {
        Map<String, Object> actionData = objectMapper.readValue(rule.getAction(), Map.class);
        String responseText = (String) actionData.get("text");
        
        if (responseText == null) {
            responseText = "Xin lỗi, tôi không hiểu yêu cầu của bạn.";
        }
        
        // Replace variables in response
        return replaceVariables(responseText, context);
    }

    /**
     * Process redirect action
     */
    private String processRedirectAction(BotRule rule, ConversationContext context) throws JsonProcessingException {
        Map<String, Object> actionData = objectMapper.readValue(rule.getAction(), Map.class);
        String redirectIntent = (String) actionData.get("intent");
        
        if (redirectIntent != null) {
            context.setLastIntent(redirectIntent);
            context.addIntentToHistory(redirectIntent);
        }
        
        return (String) actionData.getOrDefault("message", "Đang chuyển hướng...");
    }

    /**
     * Process webhook action
     */
    private String processWebhookAction(BotRule rule, ConversationContext context) {
        // This would implement webhook calling logic
        log.info("🌐 Calling webhook for rule: {}", rule.getId());
        return "Yêu cầu của bạn đã được ghi nhận và sẽ được xử lý sớm.";
    }

    /**
     * Process script action
     */
    private String processScriptAction(BotRule rule, ConversationContext context) {
        // This would implement script execution logic
        log.info("📜 Executing script for rule: {}", rule.getId());
        return "Yêu cầu của bạn đang được xử lý...";
    }

    /**
     * Replace variables in response text
     */
    private String replaceVariables(String text, ConversationContext context) {
        String result = text;
        
        // Replace common variables
        result = result.replace("{{user_name}}", context.getUserId());
        result = result.replace("{{bot_name}}", context.getBotId());
        result = result.replace("{{platform}}", context.getPlatform());
        
        // Replace metadata variables
        if (context.getMetadata() != null) {
            for (Map.Entry<String, Object> entry : context.getMetadata().entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                result = result.replace(placeholder, value);
            }
        }
        
        return result;
    }

    // Result class
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RuleEvaluationResult {
        private boolean matched;
        private BotRule rule;
        private String response;
        private BotRule.RuleType ruleType;
        private Map<String, Object> metadata;
    }
}
