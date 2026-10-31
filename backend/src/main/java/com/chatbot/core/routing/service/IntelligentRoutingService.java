package com.chatbot.core.routing.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.model.AgentWorkload;
import com.chatbot.core.routing.model.RoutingHistory;
import com.chatbot.core.routing.model.RoutingRule;
import com.chatbot.core.routing.repository.AgentWorkloadRepository;
import com.chatbot.core.routing.repository.IntelligentRoutingRuleRepository;
import com.chatbot.core.routing.repository.RoutingHistoryRepository;
import com.chatbot.core.training.model.AgentSkill;
import com.chatbot.core.training.repository.AgentSkillRepository;
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
    private final AgentSkillRepository agentSkillRepository;
    private final ConversationRepository conversationRepository;
    
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

        // Check for logical operators (AND, OR)
        if (conditions.containsKey("AND")) {
            Object andConditions = conditions.get("AND");
            if (andConditions instanceof List) {
                for (Object cond : (List<?>) andConditions) {
                    if (cond instanceof Map) {
                        if (!evaluateConditions((Map<String, Object>) cond, conversationContext)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }

        if (conditions.containsKey("OR")) {
            Object orConditions = conditions.get("OR");
            if (orConditions instanceof List) {
                for (Object cond : (List<?>) orConditions) {
                    if (cond instanceof Map) {
                        if (evaluateConditions((Map<String, Object>) cond, conversationContext)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }

        // Evaluate individual conditions with operators
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            String key = entry.getKey();
            Object expectedValue = entry.getValue();

            // Skip logical operators (already handled above)
            if ("AND".equals(key) || "OR".equals(key)) {
                continue;
            }

            Object actualValue = conversationContext.get(key);

            // Parse operator from key (e.g., "age>18", "name contains 'John'")
            if (key.contains(">")) {
                String[] parts = key.split(">");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    actualValue = conversationContext.get(field);
                    if (!evaluateComparison(actualValue, expectedValue, ">")) {
                        return false;
                    }
                }
            } else if (key.contains("<")) {
                String[] parts = key.split("<");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    actualValue = conversationContext.get(field);
                    if (!evaluateComparison(actualValue, expectedValue, "<")) {
                        return false;
                    }
                }
            } else if (key.contains(">=")) {
                String[] parts = key.split(">=");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    actualValue = conversationContext.get(field);
                    if (!evaluateComparison(actualValue, expectedValue, ">=")) {
                        return false;
                    }
                }
            } else if (key.contains("<=")) {
                String[] parts = key.split("<=");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    actualValue = conversationContext.get(field);
                    if (!evaluateComparison(actualValue, expectedValue, "<=")) {
                        return false;
                    }
                }
            } else if (key.contains("!=")) {
                String[] parts = key.split("!=");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    actualValue = conversationContext.get(field);
                    if (Objects.equals(actualValue, expectedValue)) {
                        return false;
                    }
                }
            } else if (key.contains(" contains ")) {
                String[] parts = key.split(" contains ");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    actualValue = conversationContext.get(field);
                    if (!evaluateContains(actualValue, expectedValue)) {
                        return false;
                    }
                }
            } else if (key.contains(" in ")) {
                String[] parts = key.split(" in ");
                if (parts.length == 2) {
                    String field = parts[0].trim();
                    actualValue = conversationContext.get(field);
                    if (!evaluateIn(actualValue, expectedValue)) {
                        return false;
                    }
                }
            } else {
                // Simple equality check
                if (!Objects.equals(expectedValue, actualValue)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean evaluateComparison(Object actual, Object expected, String operator) {
        if (actual == null || expected == null) {
            return false;
        }

        try {
            double actualNum = Double.parseDouble(actual.toString());
            double expectedNum = Double.parseDouble(expected.toString());

            switch (operator) {
                case ">":
                    return actualNum > expectedNum;
                case "<":
                    return actualNum < expectedNum;
                case ">=":
                    return actualNum >= expectedNum;
                case "<=":
                    return actualNum <= expectedNum;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to compare values: {} {} {}", actual, operator, expected);
            return false;
        }
    }

    private boolean evaluateContains(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return false;
        }
        String actualStr = actual.toString().toLowerCase();
        String expectedStr = expected.toString().toLowerCase();
        return actualStr.contains(expectedStr);
    }

    private boolean evaluateIn(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return false;
        }

        if (expected instanceof List) {
            return ((List<?>) expected).contains(actual);
        } else if (expected.getClass().isArray()) {
            Object[] array = (Object[]) expected;
            for (Object item : array) {
                if (Objects.equals(item, actual)) {
                    return true;
                }
            }
            return false;
        } else {
            // Treat as comma-separated string
            String[] values = expected.toString().split(",");
            for (String value : values) {
                if (value.trim().equals(actual.toString())) {
                    return true;
                }
            }
            return false;
        }
    }
    
    private RoutingAssignmentResult executeRoutingRule(RoutingRule rule, Long conversationId, Map<String, Object> context) {
        log.info("Executing routing rule {} for conversation {}", rule.getName(), conversationId);
        
        Long assignedAgentId = null;
        Long assignedTeamId = null;
        String routingStrategy = rule.getRuleType().name();
        
        switch (rule.getRuleType()) {
            case SKILL_BASED:
                assignedAgentId = routeBySkill(rule, context);
                assignedTeamId = rule.getTargetTeamId();
                break;
            case PRIORITY_BASED:
                assignedAgentId = routeByPriority(rule, context);
                assignedTeamId = rule.getTargetTeamId();
                break;
            case ROUND_ROBIN:
                assignedAgentId = routeByRoundRobin(rule);
                assignedTeamId = rule.getTargetTeamId();
                break;
            case LEAST_BUSY:
                assignedAgentId = routeByLeastBusy(rule);
                assignedTeamId = rule.getTargetTeamId();
                break;
            case CUSTOM:
                assignedAgentId = routeByCustom(rule, context);
                assignedTeamId = rule.getTargetTeamId();
                break;
            default:
                // Default to target agent/team if specified
                assignedAgentId = rule.getTargetAgentId();
                assignedTeamId = rule.getTargetTeamId();
        }

        // Assign conversation to agent if routing succeeded
        if (assignedAgentId != null) {
            assignConversationToAgent(conversationId, assignedAgentId);
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

    private void assignConversationToAgent(Long conversationId, Long agentId) {
        try {
            Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation != null) {
                conversation.setAgentAssignedId(agentId);
                conversationRepository.save(conversation);
                log.info("Conversation {} assigned to agent {}", conversationId, agentId);

                // Update agent workload
                AgentWorkload agentWorkload = agentWorkloadRepository
                        .findByTenantIdAndAgentId(tenantId, agentId)
                        .orElse(null);
                if (agentWorkload != null) {
                    agentWorkload.setActiveConversations(agentWorkload.getActiveConversations() + 1);
                    agentWorkloadRepository.save(agentWorkload);
                    log.info("Agent {} workload updated to {} active conversations",
                            agentId, agentWorkload.getActiveConversations());
                }
            } else {
                log.warn("Conversation {} not found for assignment", conversationId);
            }
        } catch (Exception e) {
            log.error("Failed to assign conversation {} to agent {}", conversationId, agentId, e);
        }
    }
    
    private Long routeBySkill(RoutingRule rule, Map<String, Object> context) {
        log.info("Applying skill-based routing for rule {}", rule.getName());

        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        Long targetSkillId = rule.getTargetSkillId();
        Long targetTeamId = rule.getTargetTeamId();

        // Find agents with the required skill who are available
        List<AgentWorkload> availableAgents = agentWorkloadRepository
                .findByTenantIdAndStatus(tenantId, AgentWorkload.AgentStatus.AVAILABLE)
                .stream()
                .filter(AgentWorkload::isAvailable)
                .filter(agent -> targetTeamId == null || targetTeamId.equals(agent.getTeamId()))
                .collect(Collectors.toList());

        if (availableAgents.isEmpty()) {
            log.warn("No available agents for skill-based routing, applying fallback logic");
            return applyFallbackLogic(rule, "skill-based");
        }

        // Real skill matching: Filter agents by skill and proficiency
        List<AgentWorkload> skilledAgents = new ArrayList<>();
        for (AgentWorkload agent : availableAgents) {
            List<AgentSkill> skills = agentSkillRepository.findByAgentId(agent.getAgentId());
            
            // Check if agent has the required skill
            boolean hasRequiredSkill = skills.stream()
                    .anyMatch(skill -> skill.getId().equals(targetSkillId) || 
                            (targetSkillId != null && skill.getSkillCategory().ordinal() == targetSkillId.intValue()));
            
            if (hasRequiredSkill) {
                skilledAgents.add(agent);
                log.debug("Agent {} has required skill", agent.getAgentId());
            }
        }

        // If no agents with specific skill, use all available agents as fallback
        List<AgentWorkload> candidateAgents = skilledAgents.isEmpty() ? availableAgents : skilledAgents;

        // Select agent with highest skill score and lowest workload
        AgentWorkload selectedAgent = candidateAgents.stream()
                .max(Comparator.comparingInt(agent -> {
                    // Calculate composite score: skill score - workload penalty
                    int skillScore = getAgentSkillScore(agent.getAgentId(), targetSkillId);
                    int workloadPenalty = agent.getActiveConversations() * 10;
                    return skillScore - workloadPenalty;
                }))
                .orElse(candidateAgents.stream()
                        .min(Comparator.comparingInt(AgentWorkload::getActiveConversations))
                        .orElse(availableAgents.get(0)));

        log.info("Selected agent {} with {} active conversations for skill-based routing",
                selectedAgent.getAgentId(), selectedAgent.getActiveConversations());

        return selectedAgent.getAgentId();
    }

    private int getAgentSkillScore(Long agentId, Long targetSkillId) {
        List<AgentSkill> skills = agentSkillRepository.findByAgentId(agentId);
        if (targetSkillId != null) {
            return skills.stream()
                    .filter(skill -> skill.getId().equals(targetSkillId) ||
                            skill.getSkillCategory().ordinal() == targetSkillId.intValue())
                    .mapToInt(AgentSkill::getScore)
                    .findFirst()
                    .orElse(50); // Default score if no matching skill
        }
        return (int) skills.stream()
                .mapToInt(AgentSkill::getScore)
                .average()
                .orElse(50); // Average score across all skills
    }
    
    private Long routeByPriority(RoutingRule rule, Map<String, Object> context) {
        log.info("Applying priority-based routing for rule {}", rule.getName());
        
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        Long targetTeamId = rule.getTargetTeamId();
        
        // Find available agents ordered by priority score
        List<AgentWorkload> availableAgents = agentWorkloadRepository
                .findAvailableAgentsByPriority(tenantId, AgentWorkload.AgentStatus.AVAILABLE)
                .stream()
                .filter(AgentWorkload::isAvailable)
                .filter(agent -> targetTeamId == null || targetTeamId.equals(agent.getTeamId()))
                .collect(Collectors.toList());
        
        if (availableAgents.isEmpty()) {
            log.warn("No available agents for priority-based routing, applying fallback logic");
            return applyFallbackLogic(rule, "priority-based");
        }
        
        // Select agent with highest priority score
        AgentWorkload selectedAgent = availableAgents.stream()
                .max(Comparator.comparingInt(AgentWorkload::getPriorityScore))
                .orElse(availableAgents.get(0));
        
        log.info("Selected agent {} with priority score {}", 
                selectedAgent.getAgentId(), selectedAgent.getPriorityScore());
        
        return selectedAgent.getAgentId();
    }
    
    private Long routeByRoundRobin(RoutingRule rule) {
        log.info("Applying round-robin routing for rule {}", rule.getName());
        
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        Long targetTeamId = rule.getTargetTeamId();
        
        // Find all available agents in the team (if teamId specified) or all available agents
        List<AgentWorkload> availableAgents;
        if (targetTeamId != null) {
            availableAgents = agentWorkloadRepository
                    .findByTenantIdAndStatus(tenantId, AgentWorkload.AgentStatus.AVAILABLE)
                    .stream()
                    .filter(agent -> targetTeamId.equals(agent.getTeamId()))
                    .filter(AgentWorkload::isAvailable)
                    .collect(Collectors.toList());
            log.info("Found {} available agents in team {}", availableAgents.size(), targetTeamId);
        } else {
            availableAgents = agentWorkloadRepository
                    .findByTenantIdAndStatus(tenantId, AgentWorkload.AgentStatus.AVAILABLE)
                    .stream()
                    .filter(AgentWorkload::isAvailable)
                    .collect(Collectors.toList());
            log.info("Found {} available agents (no team filter)", availableAgents.size());
        }
        
        if (availableAgents.isEmpty()) {
            log.warn("No available agents for round-robin routing, applying fallback logic");
            return applyFallbackLogic(rule, "round-robin");
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
        Long targetTeamId = rule.getTargetTeamId();
        
        // Find available agents ordered by workload (ascending)
        List<AgentWorkload> availableAgents = agentWorkloadRepository
                .findAvailableAgentsByWorkload(tenantId, AgentWorkload.AgentStatus.AVAILABLE)
                .stream()
                .filter(agent -> targetTeamId == null || targetTeamId.equals(agent.getTeamId()))
                .collect(Collectors.toList());
        
        if (availableAgents.isEmpty()) {
            log.warn("No available agents for least-busy routing, applying fallback logic");
            return applyFallbackLogic(rule, "least-busy");
        }
        
        // Select agent with lowest workload percentage
        AgentWorkload selectedAgent = availableAgents.stream()
                .min(Comparator.comparingDouble(AgentWorkload::getWorkloadPercentage))
                .orElse(availableAgents.get(0));
        
        log.info("Selected agent {} with workload percentage {}", 
                selectedAgent.getAgentId(), selectedAgent.getWorkloadPercentage());
        
        return selectedAgent.getAgentId();
    }
    
    private Long routeByCustom(RoutingRule rule, Map<String, Object> context) {
        // Custom routing logic based on rule conditions
        log.info("Applying custom routing for rule {}", rule.getName());
        return rule.getTargetAgentId(); // Placeholder - implement custom logic
    }

    private Long applyFallbackLogic(RoutingRule rule, String routingType) {
        log.info("Applying fallback logic for {} routing", routingType);

        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();

        // Fallback 1: Try target agent if specified
        if (rule.getTargetAgentId() != null) {
            AgentWorkload targetAgent = agentWorkloadRepository
                    .findByTenantIdAndAgentId(tenantId, rule.getTargetAgentId())
                    .orElse(null);
            if (targetAgent != null && targetAgent.isAvailable()) {
                log.info("Fallback: Using target agent {}", rule.getTargetAgentId());
                return rule.getTargetAgentId();
            }
        }

        // Fallback 2: Try target team if specified
        if (rule.getTargetTeamId() != null) {
            List<AgentWorkload> teamAgents = agentWorkloadRepository
                    .findByTenantIdAndTeamId(tenantId, rule.getTargetTeamId())
                    .stream()
                    .filter(AgentWorkload::isAvailable)
                    .collect(Collectors.toList());
            if (!teamAgents.isEmpty()) {
                AgentWorkload selected = teamAgents.stream()
                        .min(Comparator.comparingInt(AgentWorkload::getActiveConversations))
                        .orElse(teamAgents.get(0));
                log.info("Fallback: Using team agent {} from team {}", selected.getAgentId(), rule.getTargetTeamId());
                return selected.getAgentId();
            }
        }

        // Fallback 3: Try any available agent in tenant
        List<AgentWorkload> allAvailableAgents = agentWorkloadRepository
                .findByTenantIdAndStatus(tenantId, AgentWorkload.AgentStatus.AVAILABLE)
                .stream()
                .filter(AgentWorkload::isAvailable)
                .collect(Collectors.toList());
        if (!allAvailableAgents.isEmpty()) {
            AgentWorkload selected = allAvailableAgents.stream()
                    .min(Comparator.comparingInt(AgentWorkload::getActiveConversations))
                    .orElse(allAvailableAgents.get(0));
            log.info("Fallback: Using any available agent {}", selected.getAgentId());
            return selected.getAgentId();
        }

        // Fallback 4: No agents available - return null to indicate routing failure
        log.error("Fallback failed: No agents available for routing");
        return null;
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
