package com.chatbot.core.routing.service;

import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.model.RoutingRule;
import com.chatbot.core.routing.repository.IntelligentRoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntelligentRoutingService {

    private final IntelligentRoutingRuleRepository routingRuleRepository;

    @Transactional
    public RoutingRuleDTO createRoutingRule(CreateRoutingRuleRequest request) {
        log.info("Creating routing rule: {}", request.getName());

        RoutingRule routingRule = RoutingRule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .ruleType(request.getRuleType())
                .priority(request.getPriority())
                .targetType(request.getTargetType())
                .targetAgentId(request.getTargetAgentId())
                .targetTeamId(request.getTargetTeamId())
                .targetSkillId(request.getTargetSkillId())
                .conditions(request.getConditions())
                .maxConcurrentConversations(request.getMaxConcurrentConversations())
                .isActive(request.getIsActive())
                .isDefault(request.getIsDefault())
                .build();

        routingRule.setTenantId(com.chatbot.core.tenant.infra.TenantContext.getTenantId());
        RoutingRule saved = routingRuleRepository.save(routingRule);

        log.info("Routing rule created successfully with ID: {}", saved.getId());
        return mapToDTO(saved);
    }

    @Transactional
    public RoutingRuleDTO updateRoutingRule(Long id, UpdateRoutingRuleRequest request) {
        log.info("Updating routing rule with ID: {}", id);

        RoutingRule routingRule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Routing rule not found"));

        routingRule.setName(request.getName());
        routingRule.setDescription(request.getDescription());
        if (request.getRuleType() != null) {
            routingRule.setRuleType(request.getRuleType());
        }
        if (request.getPriority() != null) {
            routingRule.setPriority(request.getPriority());
        }
        if (request.getTargetType() != null) {
            routingRule.setTargetType(request.getTargetType());
        }
        routingRule.setTargetAgentId(request.getTargetAgentId());
        routingRule.setTargetTeamId(request.getTargetTeamId());
        routingRule.setTargetSkillId(request.getTargetSkillId());
        routingRule.setConditions(request.getConditions());
        routingRule.setMaxConcurrentConversations(request.getMaxConcurrentConversations());
        if (request.getIsActive() != null) {
            routingRule.setIsActive(request.getIsActive());
        }
        if (request.getIsDefault() != null) {
            routingRule.setIsDefault(request.getIsDefault());
        }

        RoutingRule updated = routingRuleRepository.save(routingRule);
        log.info("Routing rule updated successfully");
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteRoutingRule(Long id) {
        log.info("Deleting routing rule with ID: {}", id);
        RoutingRule routingRule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Routing rule not found"));
        routingRuleRepository.delete(routingRule);
        log.info("Routing rule deleted successfully");
    }

    @Transactional(readOnly = true)
    public RoutingRuleDTO getRoutingRuleById(Long id) {
        RoutingRule routingRule = routingRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Routing rule not found"));
        return mapToDTO(routingRule);
    }

    @Transactional(readOnly = true)
    public List<RoutingRuleDTO> getAllRoutingRules() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<RoutingRule> rules = routingRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(tenantId);
        return rules.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoutingRuleDTO> getRoutingRulesByType(RoutingRule.RoutingRuleType ruleType) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<RoutingRule> rules = routingRuleRepository.findByTenantIdAndRuleTypeAndIsActiveTrue(tenantId, ruleType);
        return rules.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoutingRuleDTO> getRoutingRulesByTargetType(RoutingRule.TargetType targetType) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<RoutingRule> rules = routingRuleRepository.findByTenantIdAndTargetTypeAndIsActiveTrue(tenantId, targetType);
        return rules.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoutingRuleDTO getDefaultRoutingRule() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<RoutingRule> defaultRules = routingRuleRepository.findByTenantIdAndIsActiveTrueAndIsDefaultTrue(tenantId);
        if (!defaultRules.isEmpty()) {
            return mapToDTO(defaultRules.get(0));
        }
        return routingRuleRepository.findFirstByTenantIdAndIsActiveTrueOrderByPriorityAsc(tenantId)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("No active routing rule found"));
    }

    private RoutingRuleDTO mapToDTO(RoutingRule routingRule) {
        return RoutingRuleDTO.builder()
                .id((Long) routingRule.getId())
                .name(routingRule.getName())
                .description(routingRule.getDescription())
                .ruleType(routingRule.getRuleType())
                .priority(routingRule.getPriority())
                .targetType(routingRule.getTargetType())
                .targetAgentId(routingRule.getTargetAgentId())
                .targetTeamId(routingRule.getTargetTeamId())
                .targetSkillId(routingRule.getTargetSkillId())
                .conditions(routingRule.getConditions())
                .maxConcurrentConversations(routingRule.getMaxConcurrentConversations())
                .isActive(routingRule.getIsActive())
                .isDefault(routingRule.getIsDefault())
                .createdAt(routingRule.getCreatedAt())
                .updatedAt(routingRule.getUpdatedAt())
                .build();
    }

    @Transactional
    public RoutingAssignmentResult routeConversation(Long conversationId, Map<String, Object> conversationContext) {
        log.info("Routing conversation {} with context: {}", conversationId, conversationContext);
        
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        
        // Get all active routing rules ordered by priority
        List<RoutingRule> rules = routingRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(tenantId);
        
        if (rules.isEmpty()) {
            log.warn("No active routing rules found for tenant {}", tenantId);
            return RoutingAssignmentResult.builder()
                    .conversationId(conversationId)
                    .assigned(false)
                    .reason("No active routing rules")
                    .build();
        }
        
        // Evaluate rules in priority order
        for (RoutingRule rule : rules) {
            if (evaluateConditions(rule.getConditions(), conversationContext)) {
                return executeRoutingRule(rule, conversationId, conversationContext);
            }
        }
        
        // If no rule matches, use default rule
        RoutingRule defaultRule = rules.stream()
                .filter(RoutingRule::getIsDefault)
                .findFirst()
                .orElse(rules.get(0));
        
        return executeRoutingRule(defaultRule, conversationId, conversationContext);
    }
    
    private boolean evaluateConditions(Map<String, Object> conditions, Map<String, Object> conversationContext) {
        if (conditions == null || conditions.isEmpty()) {
            return true; // No conditions means always match
        }
        
        // Simple condition evaluation - can be enhanced with complex logic
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String key = entry.getKey();
            Object expectedValue = entry.getValue();
            Object actualValue = conversationContext.get(key);
            
            if (!Objects.equals(expectedValue, actualValue)) {
                return false;
            }
        }
        
        return true;
    }
    
    private RoutingAssignmentResult executeRoutingRule(RoutingRule rule, Long conversationId, Map<String, Object> context) {
        log.info("Executing routing rule {} for conversation {}", rule.getName(), conversationId);
        
        Long assignedAgentId = null;
        Long assignedTeamId = null;
        String routingStrategy = rule.getRuleType().name();
        
        switch (rule.getRuleType()) {
            case SKILL_BASED:
                assignedAgentId = routeBySkill(rule, context);
                break;
            case PRIORITY_BASED:
                assignedAgentId = routeByPriority(rule, context);
                break;
            case ROUND_ROBIN:
                assignedAgentId = routeByRoundRobin(rule);
                break;
            case LEAST_BUSY:
                assignedAgentId = routeByLeastBusy(rule);
                break;
            case CUSTOM:
                assignedAgentId = routeByCustom(rule, context);
                break;
            default:
                // Default to target agent/team if specified
                assignedAgentId = rule.getTargetAgentId();
                assignedTeamId = rule.getTargetTeamId();
        }
        
        return RoutingAssignmentResult.builder()
                .conversationId(conversationId)
                .assigned(assignedAgentId != null || assignedTeamId != null)
                .assignedAgentId(assignedAgentId)
                .assignedTeamId(assignedTeamId)
                .routingRuleId((Long) rule.getId())
                .routingStrategy(routingStrategy)
                .assignedAt(LocalDateTime.now())
                .reason("Routing rule matched: " + rule.getName())
                .build();
    }
    
    private Long routeBySkill(RoutingRule rule, Map<String, Object> context) {
        // Skill-based routing logic
        // In a real implementation, this would query agent skills and match with conversation requirements
        log.info("Applying skill-based routing for rule {}", rule.getName());
        return rule.getTargetAgentId(); // Placeholder - implement actual skill matching
    }
    
    private Long routeByPriority(RoutingRule rule, Map<String, Object> context) {
        // Priority-based routing logic
        // Route to highest priority agent available
        log.info("Applying priority-based routing for rule {}", rule.getName());
        return rule.getTargetAgentId(); // Placeholder - implement actual priority logic
    }
    
    private Long routeByRoundRobin(RoutingRule rule) {
        // Round-robin routing logic
        // Distribute conversations evenly among agents
        log.info("Applying round-robin routing for rule {}", rule.getName());
        return rule.getTargetAgentId(); // Placeholder - implement actual round-robin logic
    }
    
    private Long routeByLeastBusy(RoutingRule rule) {
        // Least-busy routing logic
        // Route to agent with fewest active conversations
        log.info("Applying least-busy routing for rule {}", rule.getName());
        return rule.getTargetAgentId(); // Placeholder - implement actual workload tracking
    }
    
    private Long routeByCustom(RoutingRule rule, Map<String, Object> context) {
        // Custom routing logic based on rule conditions
        log.info("Applying custom routing for rule {}", rule.getName());
        return rule.getTargetAgentId(); // Placeholder - implement custom logic
    }
    
    @Transactional(readOnly = true)
    public List<RoutingAssignmentResult> getRoutingHistory(Long conversationId) {
        // Placeholder for retrieving routing history
        log.info("Retrieving routing history for conversation {}", conversationId);
        return Collections.emptyList();
    }
}
