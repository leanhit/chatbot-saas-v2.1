package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.RoutingRule;
import com.chatbot.core.message.store.repository.RoutingRuleRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing and applying routing rules
 * Implements Phase 1.3: Attribute-based Routing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingRuleService {

    private final RoutingRuleRepository routingRuleRepository;
    private final AgentAssignmentService agentAssignmentService;
    private final ConversationRepository conversationRepository;
    private final EscalationService escalationService;

    /**
     * Apply routing rules to a conversation
     * Returns true if a rule was matched and applied
     */
    @Transactional
    public boolean applyRoutingRules(Conversation conversation) {
        List<RoutingRule> rules = routingRuleRepository.findByTenantIdAndActiveOrderByPriorityDesc(
            conversation.getTenantId(),
            true
        );

        if (rules.isEmpty()) {
            log.debug("No active routing rules for tenant {}", conversation.getTenantId());
            return false;
        }

        // Build conversation attributes map
        Map<String, Object> attributes = buildConversationAttributes(conversation);

        // Try to match rules in priority order
        for (RoutingRule rule : rules) {
            if (matchesRule(rule, attributes)) {
                log.info("Routing rule matched for conversation {}: {}", conversation.getId(), rule.getName());
                return applyRuleAction(conversation, rule);
            }
        }

        log.debug("No routing rules matched for conversation {}", conversation.getId());
        return false;
    }

    /**
     * Build attributes map from conversation for rule matching
     */
    private Map<String, Object> buildConversationAttributes(Conversation conversation) {
        Map<String, Object> attributes = new HashMap<>();
        
        attributes.put("customerTier", conversation.getCustomerTier());
        attributes.put("language", conversation.getLanguage());
        attributes.put("channel", conversation.getChannel());
        attributes.put("status", conversation.getStatus());
        attributes.put("isTakenOverByAgent", conversation.getIsTakenOverByAgent());
        
        // Parse custom attributes if present
        if (conversation.getCustomAttributes() != null) {
            try {
                Map<String, Object> customAttrs = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    conversation.getCustomAttributes(),
                    Map.class
                );
                attributes.putAll(customAttrs);
            } catch (Exception e) {
                log.error("Error parsing custom attributes", e);
            }
        }

        return attributes;
    }

    /**
     * Check if conversation attributes match rule conditions
     */
    private boolean matchesRule(RoutingRule rule, Map<String, Object> attributes) {
        Map<String, Object> conditions = rule.getConditions();
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }

        for (Map.Entry<String, Object> condition : conditions.entrySet()) {
            String key = condition.getKey();
            Object expectedValue = condition.getValue();
            Object actualValue = attributes.get(key);

            if (actualValue == null) {
                if (expectedValue != null) {
                    return false;
                }
            } else if (!actualValue.equals(expectedValue)) {
                // Handle special cases like "contains", ">", "<", etc.
                if (!matchesSpecialCondition(actualValue, expectedValue, key)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Handle special condition matching (contains, greater than, etc.)
     */
    private boolean matchesSpecialCondition(Object actualValue, Object expectedValue, String key) {
        String actualStr = actualValue.toString().toLowerCase();
        String expectedStr = expectedValue.toString().toLowerCase();

        // Check for special operators in expected value
        if (expectedStr.startsWith("contains:")) {
            return actualStr.contains(expectedStr.substring(9));
        }
        if (expectedStr.startsWith("startsWith:")) {
            return actualStr.startsWith(expectedStr.substring(11));
        }
        if (expectedStr.startsWith("endsWith:")) {
            return actualStr.endsWith(expectedStr.substring(8));
        }

        // Default to exact match
        return actualStr.equals(expectedStr);
    }

    /**
     * Apply the action specified by the routing rule
     */
    private boolean applyRuleAction(Conversation conversation, RoutingRule rule) {
        Map<String, Object> action = rule.getAction();
        if (action == null) {
            log.warn("Rule {} has no action defined", rule.getName());
            return false;
        }

        String actionType = (String) action.get("action");
        if (actionType == null) {
            log.warn("Rule {} action has no type defined", rule.getName());
            return false;
        }

        try {
            switch (actionType) {
                case "assign_to_agent":
                    return handleAssignToAgent(conversation, action);
                case "route_to_queue":
                    return handleRouteToQueue(conversation, action);
                case "escalate":
                    return handleEscalate(conversation, action);
                case "block":
                    return handleBlock(conversation);
                case "custom":
                    return handleCustomAction(conversation, action);
                default:
                    log.warn("Unknown action type: {}", actionType);
                    return false;
            }
        } catch (Exception e) {
            log.error("Error applying rule action: {}", actionType, e);
            return false;
        }
    }

    /**
     * Handle assign to agent action
     */
    private boolean handleAssignToAgent(Conversation conversation, Map<String, Object> action) {
        Long agentId = ((Number) action.get("agentId")).longValue();
        
        if (agentAssignmentService != null) {
            boolean success = agentAssignmentService.reassignConversation(conversation.getId(), agentId);
            if (success) {
                log.info("Routing rule assigned conversation {} to agent {}", conversation.getId(), agentId);
                return true;
            }
        }
        return false;
    }

    /**
     * Handle route to queue action
     */
    private boolean handleRouteToQueue(Conversation conversation, Map<String, Object> action) {
        String queueName = (String) action.get("queueName");
        log.info("Routing rule routed conversation {} to queue: {}", conversation.getId(), queueName);
        // TODO: Implement queue routing logic
        return true;
    }

    /**
     * Handle escalate action
     */
    private boolean handleEscalate(Conversation conversation, Map<String, Object> action) {
        String escalationTier = (String) action.get("tier");
        log.info("Routing rule escalated conversation {} to tier: {}", conversation.getId(), escalationTier);
        if (escalationService != null) {
            escalationService.escalateConversationToTier(conversation, escalationTier);
        }
        return true;
    }

    /**
     * Handle block action
     */
    private boolean handleBlock(Conversation conversation) {
        log.info("Routing rule blocked conversation {}", conversation.getId());
        conversation.setStatus("blocked");
        conversationRepository.save(conversation);
        return true;
    }

    /**
     * Handle custom action
     */
    private boolean handleCustomAction(Conversation conversation, Map<String, Object> action) {
        String customAction = (String) action.get("customAction");
        log.info("Routing rule applied custom action {} to conversation {}", customAction, conversation.getId());
        
        // Store custom action metadata in conversation for later processing
        conversation.setCustomAction(customAction);
        // Convert Map to JSON string for storage
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            conversation.setCustomActionData(mapper.writeValueAsString(action));
        } catch (Exception e) {
            log.error("Failed to convert custom action data to JSON: {}", e.getMessage());
        }
        conversationRepository.save(conversation);
        
        return true;
    }

    /**
     * Create a new routing rule
     */
    @Transactional
    public RoutingRule createRoutingRule(RoutingRule rule) {
        return routingRuleRepository.save(rule);
    }

    /**
     * Update an existing routing rule
     */
    @Transactional
    public RoutingRule updateRoutingRule(Long ruleId, RoutingRule updatedRule) {
        return routingRuleRepository.findById(ruleId)
            .map(existingRule -> {
                existingRule.setName(updatedRule.getName());
                existingRule.setDescription(updatedRule.getDescription());
                existingRule.setPriority(updatedRule.getPriority());
                existingRule.setConditions(updatedRule.getConditions());
                existingRule.setAction(updatedRule.getAction());
                existingRule.setActive(updatedRule.getActive());
                existingRule.setRuleType(updatedRule.getRuleType());
                return routingRuleRepository.save(existingRule);
            })
            .orElseThrow(() -> new RuntimeException("Routing rule not found: " + ruleId));
    }

    /**
     * Delete a routing rule
     */
    @Transactional
    public void deleteRoutingRule(Long ruleId) {
        routingRuleRepository.deleteById(ruleId);
    }

    /**
     * Get all routing rules for a tenant
     */
    public List<RoutingRule> getRoutingRules(Long tenantId) {
        return routingRuleRepository.findByTenantId(tenantId);
    }

    /**
     * Get active routing rules for a tenant
     */
    public List<RoutingRule> getActiveRoutingRules(Long tenantId) {
        return routingRuleRepository.findByTenantIdAndActiveOrderByPriorityDesc(tenantId, true);
    }

    /**
     * Create default routing rules for a tenant
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public void createDefaultRoutingRules(Long tenantId) {
        if (routingRuleRepository.findByTenantId(tenantId).isEmpty()) {
            // Rule 1: VIP customers auto-assign to available agents
            Map<String, Object> vipConditions = new HashMap<>();
            vipConditions.put("customerTier", "VIP");
            
            Map<String, Object> vipAction = new HashMap<>();
            vipAction.put("action", "route_to_queue");
            vipAction.put("queueName", "vip_queue");

            RoutingRule vipRule = RoutingRule.builder()
                .tenantId(tenantId)
                .name("VIP Auto-Route")
                .description("Auto-route VIP customers to VIP queue")
                .priority(100)
                .conditions(vipConditions)
                .action(vipAction)
                .active(true)
                .ruleType(RoutingRule.RoutingRuleType.ROUTE_TO_QUEUE)
                .build();

            // Rule 2: Enterprise customers auto-assign
            Map<String, Object> enterpriseConditions = new HashMap<>();
            enterpriseConditions.put("customerTier", "Enterprise");
            
            Map<String, Object> enterpriseAction = new HashMap<>();
            enterpriseAction.put("action", "route_to_queue");
            enterpriseAction.put("queueName", "enterprise_queue");

            RoutingRule enterpriseRule = RoutingRule.builder()
                .tenantId(tenantId)
                .name("Enterprise Auto-Route")
                .description("Auto-route Enterprise customers to Enterprise queue")
                .priority(90)
                .conditions(enterpriseConditions)
                .action(enterpriseAction)
                .active(true)
                .ruleType(RoutingRule.RoutingRuleType.ROUTE_TO_QUEUE)
                .build();

            routingRuleRepository.save(vipRule);
            routingRuleRepository.save(enterpriseRule);

            log.info("Created default routing rules for tenant {}", tenantId);
        }
    }
}
