package com.chatbot.core.routing.service;

import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.model.AgentWorkload;
import com.chatbot.core.routing.model.RoutingHistory;
import com.chatbot.core.routing.model.RoutingRule;
import com.chatbot.core.routing.repository.AgentWorkloadRepository;
import com.chatbot.core.routing.repository.IntelligentRoutingRuleRepository;
import com.chatbot.core.routing.repository.RoutingHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntelligentRoutingService {

    private final IntelligentRoutingRuleRepository routingRuleRepository;
    private final AgentWorkloadRepository agentWorkloadRepository;
    private final RoutingHistoryRepository routingHistoryRepository;
    
    // Round-robin tracking per team
    private final Map<Long, Long> roundRobinIndex = new ConcurrentHashMap<>();

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
        
        // Save routing history
        saveRoutingHistory(conversationId, assignedAgentId, assignedTeamId, rule, context);
        
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
        log.info("Applying skill-based routing for rule {}", rule.getName());
        
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        Long targetSkillId = rule.getTargetSkillId();
        
        // Find agents with the required skill who are available
        List<AgentWorkload> availableAgents = agentWorkloadRepository
                .findByTenantIdAndStatus(tenantId, AgentWorkload.AgentStatus.AVAILABLE)
                .stream()
                .filter(AgentWorkload::isAvailable)
                .collect(Collectors.toList());
        
        if (availableAgents.isEmpty()) {
            log.warn("No available agents for skill-based routing");
            return rule.getTargetAgentId(); // Fallback to target agent
        }
        
        // In a real implementation, you would check agent skills against targetSkillId
        // For now, select the agent with lowest workload
        AgentWorkload selectedAgent = availableAgents.stream()
                .min(Comparator.comparingInt(AgentWorkload::getActiveConversations))
                .orElse(availableAgents.get(0));
        
        log.info("Selected agent {} with {} active conversations", 
                selectedAgent.getAgentId(), selectedAgent.getActiveConversations());
        
        return selectedAgent.getAgentId();
    }
    
    private Long routeByPriority(RoutingRule rule, Map<String, Object> context) {
        log.info("Applying priority-based routing for rule {}", rule.getName());
        
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        
        // Find available agents ordered by priority score
        List<AgentWorkload> availableAgents = agentWorkloadRepository
                .findAvailableAgentsByPriority(tenantId, AgentWorkload.AgentStatus.AVAILABLE)
                .stream()
                .filter(AgentWorkload::isAvailable)
                .collect(Collectors.toList());
        
        if (availableAgents.isEmpty()) {
            log.warn("No available agents for priority-based routing");
            return rule.getTargetAgentId(); // Fallback to target agent
        }
        
        // Select the highest priority agent
        AgentWorkload selectedAgent = availableAgents.get(0);
        
        log.info("Selected agent {} with priority score {}", 
                selectedAgent.getAgentId(), selectedAgent.getPriorityScore());
        
        return selectedAgent.getAgentId();
    }
    
    private Long routeByRoundRobin(RoutingRule rule) {
        log.info("Applying round-robin routing for rule {}", rule.getName());
        
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        Long targetTeamId = rule.getTargetTeamId();
        
        // Find all available agents in the team
        List<AgentWorkload> availableAgents = agentWorkloadRepository
                .findByTenantIdAndStatus(tenantId, AgentWorkload.AgentStatus.AVAILABLE)
                .stream()
                .filter(AgentWorkload::isAvailable)
                .collect(Collectors.toList());
        
        if (availableAgents.isEmpty()) {
            log.warn("No available agents for round-robin routing");
            return rule.getTargetAgentId(); // Fallback to target agent
        }
        
        // Get or initialize round-robin index for this team
        Long teamKey = targetTeamId != null ? targetTeamId : 0L;
        long currentIndex = roundRobinIndex.getOrDefault(teamKey, 0L);
        
        // Select agent using round-robin
        int selectedIndex = (int) (currentIndex % availableAgents.size());
        AgentWorkload selectedAgent = availableAgents.get(selectedIndex);
        
        // Update round-robin index
        roundRobinIndex.put(teamKey, currentIndex + 1);
        
        log.info("Round-robin selected agent {} (index {}/{})", 
                selectedAgent.getAgentId(), selectedIndex, availableAgents.size());
        
        return selectedAgent.getAgentId();
    }
    
    private Long routeByLeastBusy(RoutingRule rule) {
        log.info("Applying least-busy routing for rule {}", rule.getName());
        
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        
        // Find available agents ordered by workload (ascending)
        List<AgentWorkload> availableAgents = agentWorkloadRepository
                .findAvailableAgentsByWorkload(tenantId, AgentWorkload.AgentStatus.AVAILABLE);
        
        if (availableAgents.isEmpty()) {
            log.warn("No available agents for least-busy routing");
            return rule.getTargetAgentId(); // Fallback to target agent
        }
        
        // Select the agent with lowest workload
        AgentWorkload selectedAgent = availableAgents.get(0);
        
        log.info("Selected least-busy agent {} with {} active conversations ({}% capacity)", 
                selectedAgent.getAgentId(), 
                selectedAgent.getActiveConversations(),
                String.format("%.1f", selectedAgent.getWorkloadPercentage()));
        
        return selectedAgent.getAgentId();
    }
    
    private Long routeByCustom(RoutingRule rule, Map<String, Object> context) {
        // Custom routing logic based on rule conditions
        log.info("Applying custom routing for rule {}", rule.getName());
        return rule.getTargetAgentId(); // Placeholder - implement custom logic
    }
    
    private void saveRoutingHistory(Long conversationId, Long assignedAgentId, Long assignedTeamId, 
                                    RoutingRule rule, Map<String, Object> context) {
        try {
            Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
            
            RoutingHistory history = RoutingHistory.builder()
                    .tenantId(tenantId)
                    .conversationId(conversationId)
                    .assignedAgentId(assignedAgentId)
                    .assignedTeamId(assignedTeamId)
                    .routingRuleId((Long) rule.getId())
                    .routingStrategy(rule.getRuleType().name())
                    .conditions(rule.getConditions() != null ? rule.getConditions().toString() : null)
                    .assigned(assignedAgentId != null || assignedTeamId != null)
                    .reason("Routing rule matched: " + rule.getName())
                    .build();
            
            routingHistoryRepository.save(history);
            log.info("Routing history saved for conversation {}", conversationId);
        } catch (Exception e) {
            log.error("Failed to save routing history for conversation {}", conversationId, e);
            // Don't throw exception - routing should continue even if history save fails
        }
    }
    
    @Transactional(readOnly = true)
    public List<RoutingAssignmentResult> getRoutingHistory(Long conversationId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<RoutingHistory> historyList = routingHistoryRepository
                .findByTenantIdAndConversationIdOrderByCreatedAtDesc(tenantId, conversationId);
        
        return historyList.stream()
                .map(history -> RoutingAssignmentResult.builder()
                        .conversationId(history.getConversationId())
                        .assigned(history.getAssigned())
                        .assignedAgentId(history.getAssignedAgentId())
                        .assignedTeamId(history.getAssignedTeamId())
                        .routingRuleId(history.getRoutingRuleId())
                        .routingStrategy(history.getRoutingStrategy())
                        .assignedAt(history.getCreatedAt())
                        .reason(history.getReason())
                        .build())
                .collect(Collectors.toList());
    }
}
