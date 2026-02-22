package com.chatbot.shared.penny.rules;

import com.chatbot.shared.penny.context.ConversationContext;
import com.chatbot.shared.penny.dto.request.MiddlewareRequest;
import com.chatbot.shared.penny.dto.response.MiddlewareResponse;
import com.chatbot.shared.penny.routing.dto.IntentAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Custom Logic Engine - Tích hợp custom logic vào Penny Middleware
 */
@Service
@Slf4j
public class CustomLogicEngine {

    private final BotRuleManager botRuleManager;
    private final ResponseTemplateManager responseTemplateManager;

    public CustomLogicEngine(BotRuleManager botRuleManager,
                            ResponseTemplateManager responseTemplateManager) {
        this.botRuleManager = botRuleManager;
        this.responseTemplateManager = responseTemplateManager;
    }

    /**
     * Process message with custom logic
     */
    public MiddlewareResponse processWithCustomLogic(MiddlewareRequest request, 
                                                    ConversationContext context,
                                                    IntentAnalysisResult intentAnalysis) {
        
        String botId = request.getBotId();
        String intent = intentAnalysis.getPrimaryIntent();
        String message = request.getMessage();
        
        log.debug("🧠 Processing with custom logic for bot: {}, intent: {}", botId, intent);
        
        try {
            // Step 1: Evaluate custom rules
            BotRuleManager.RuleEvaluationResult ruleResult = botRuleManager.evaluateRules(
                UUID.fromString(botId), intent, message, context);
            
            if (ruleResult.isMatched()) {
                log.info("🎯 Custom rule matched: {}", ruleResult.getRule().getName());
                
                return MiddlewareResponse.builder()
                    .requestId(request.getRequestId())
                    .response(ruleResult.getResponse())
                    .providerUsed("CUSTOM_RULE")
                    .intentAnalysis(intentAnalysis)
                    .processingMetrics(MiddlewareResponse.ProcessingMetrics.builder()
                        .providerType("CUSTOM_RULE")
                        .selectionReason("Custom rule matched: " + ruleResult.getRule().getName())
                        .confidence(1.0)
                        .build())
                    .status("success")
                    .timestamp(java.time.Instant.now())
                    .shouldSendResponse(true)
                    .build();
            }
            
            // Step 2: Try response templates
            ResponseTemplate template = responseTemplateManager.getBestTemplate(
                UUID.fromString(botId), intent, context.getLanguage() != null ? context.getLanguage() : "vi");
            
            if (template != null) {
                log.info("📝 Response template found: {}", template.getName());
                
                // Process template with context variables
                Map<String, Object> templateVariables = buildTemplateVariables(context, request);
                ResponseTemplateManager.TemplateProcessResult templateResult = 
                    responseTemplateManager.processTemplate(template.getId(), templateVariables);
                
                return MiddlewareResponse.builder()
                    .requestId(request.getRequestId())
                    .response(templateResult.getResponse())
                    .providerUsed("CUSTOM_TEMPLATE")
                    .intentAnalysis(intentAnalysis)
                    .processingMetrics(MiddlewareResponse.ProcessingMetrics.builder()
                        .providerType("CUSTOM_TEMPLATE")
                        .selectionReason("Template matched: " + template.getName())
                        .confidence(0.9)
                        .build())
                    .status("success")
                    .timestamp(java.time.Instant.now())
                    .shouldSendResponse(true)
                    .quickReplies(convertToQuickReplies(templateResult.getQuickReplies()))
                    .attachments(convertToAttachments(templateResult.getAttachments()))
                    .build();
            }
            
            log.debug("❌ No custom logic matched for intent: {}", intent);
            return null; // Let fallback to default providers
            
        } catch (Exception e) {
            log.error("❌ Error in custom logic engine: {}", e.getMessage(), e);
            return null; // Let fallback handle the error
        }
    }

    /**
     * Build template variables from context and request
     */
    private Map<String, Object> buildTemplateVariables(ConversationContext context, MiddlewareRequest request) {
        Map<String, Object> variables = new java.util.HashMap<>();
        
        // User information
        variables.put("user_id", request.getUserId());
        variables.put("platform", request.getPlatform());
        variables.put("message", request.getMessage());
        variables.put("language", request.getLanguage());
        
        // Context information
        variables.put("bot_id", context.getBotId());
        variables.put("session_id", context.getSessionId());
        variables.put("message_count", context.getMessageCount());
        variables.put("last_intent", context.getLastIntent());
        variables.put("last_provider", context.getLastProvider());
        
        // Add context metadata
        if (context.getMetadata() != null) {
            variables.putAll(context.getMetadata());
        }
        
        // Add request metadata
        if (request.getMetadata() != null) {
            variables.putAll(request.getMetadata());
        }
        
        // Add user preferences
        if (context.getUserPreferences() != null) {
            variables.putAll(context.getUserPreferences());
        }
        
        // Add session data
        if (context.getSessionData() != null) {
            variables.putAll(context.getSessionData());
        }
        
        return variables;
    }
    
    /**
     * Convert Map<String, Object> to QuickReply list
     */
    private List<MiddlewareResponse.QuickReply> convertToQuickReplies(List<Map<String, Object>> quickRepliesMap) {
        if (quickRepliesMap == null) {
            return new java.util.ArrayList<>();
        }
        
        return quickRepliesMap.stream()
            .map(map -> MiddlewareResponse.QuickReply.builder()
                .title((String) map.get("title"))
                .payload((String) map.get("payload"))
                .imageUrl((String) map.get("imageUrl"))
                .metadata((Map<String, Object>) map.getOrDefault("metadata", new java.util.HashMap<>()))
                .build())
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Convert Map<String, Object> to Attachment list
     */
    private List<MiddlewareResponse.Attachment> convertToAttachments(List<Map<String, Object>> attachmentsMap) {
        if (attachmentsMap == null) {
            return new java.util.ArrayList<>();
        }
        
        return attachmentsMap.stream()
            .map(map -> MiddlewareResponse.Attachment.builder()
                .type((String) map.get("type"))
                .url((String) map.get("url"))
                .filename((String) map.get("filename"))
                .size((Integer) map.get("size"))
                .mimeType((String) map.get("mimeType"))
                .build())
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Check if bot has custom logic
     */
    public boolean hasCustomLogic(UUID botId) {
        long ruleCount = botRuleManager.getRulesForBot(botId).size();
        long templateCount = responseTemplateManager.getTemplatesForBot(botId).size();
        
        return ruleCount > 0 || templateCount > 0;
    }

    /**
     * Get custom logic statistics
     */
    public CustomLogicStatistics getCustomLogicStatistics(UUID botId) {
        long ruleCount = botRuleManager.getRulesForBot(botId).size();
        long activeRuleCount = botRuleManager.getRulesForBot(botId).stream()
            .mapToLong(r -> r.getIsActive() ? 1 : 0).sum();
        
        long templateCount = responseTemplateManager.getTemplatesForBot(botId).size();
        long activeTemplateCount = responseTemplateManager.getTemplatesForBot(botId).stream()
            .mapToLong(t -> t.getIsActive() ? 1 : 0).sum();
        
        long totalRuleExecutions = botRuleManager.getRulesForBot(botId).stream()
            .mapToLong(BotRule::getExecutionCount).sum();
        
        long totalTemplateUsage = responseTemplateManager.getTemplatesForBot(botId).stream()
            .mapToLong(ResponseTemplate::getUsageCount).sum();
        
        return CustomLogicStatistics.builder()
            .totalRules(ruleCount)
            .activeRules(activeRuleCount)
            .totalTemplates(templateCount)
            .activeTemplates(activeTemplateCount)
            .totalRuleExecutions(totalRuleExecutions)
            .totalTemplateUsage(totalTemplateUsage)
            .hasCustomLogic(hasCustomLogic(botId))
            .build();
    }

    /**
     * Enable/disable custom logic for bot
     */
    public boolean toggleCustomLogic(UUID botId, boolean enabled) {
        try {
            // Enable/disable all rules
            botRuleManager.getRulesForBot(botId).forEach(rule -> {
                botRuleManager.updateRule(rule.getId(), null, null, null, null, null, enabled, "system");
            });
            
            // Enable/disable all templates
            responseTemplateManager.getTemplatesForBot(botId).forEach(template -> {
                responseTemplateManager.updateTemplate(template.getId(), null, null, null, null, null, null, enabled ? 1 : 0, enabled, null, null, "system");
            });
            
            log.info("✅ Custom logic {} for bot: {}", enabled ? "enabled" : "disabled", botId);
            return true;
            
        } catch (Exception e) {
            log.error("❌ Error toggling custom logic for bot {}: {}", botId, e.getMessage(), e);
            return false;
        }
    }

    // Statistics class
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CustomLogicStatistics {
        private long totalRules;
        private long activeRules;
        private long totalTemplates;
        private long activeTemplates;
        private long totalRuleExecutions;
        private long totalTemplateUsage;
        private boolean hasCustomLogic;
    }
}
