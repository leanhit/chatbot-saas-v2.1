package com.chatbot.core.routing.service;

import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.model.EscalationRule;
import com.chatbot.core.routing.repository.IntelligentEscalationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntelligentEscalationService {

    private final IntelligentEscalationRuleRepository escalationRuleRepository;

    @Transactional
    public EscalationRuleDTO createEscalationRule(CreateEscalationRuleRequest request) {
        log.info("Creating escalation rule: {}", request.getName());

        EscalationRule escalationRule = EscalationRule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .triggerType(request.getTriggerType())
                .triggerValue(request.getTriggerValue())
                .thresholdValue(request.getThresholdValue())
                .timeThresholdMinutes(request.getTimeThresholdMinutes())
                .conditions(request.getConditions())
                .escalationAction(request.getEscalationAction())
                .escalateToAgentId(request.getEscalateToAgentId())
                .escalateToTeamId(request.getEscalateToTeamId())
                .escalateToSupervisor(request.getEscalateToSupervisor())
                .notifySupervisor(request.getNotifySupervisor())
                .priority(request.getPriority())
                .isActive(request.getIsActive())
                .build();

        escalationRule.setTenantId(com.chatbot.core.tenant.infra.TenantContext.getTenantId());
        EscalationRule saved = escalationRuleRepository.save(escalationRule);

        log.info("Escalation rule created successfully with ID: {}", saved.getId());
        return mapToDTO(saved);
    }

    @Transactional
    public EscalationRuleDTO updateEscalationRule(Long id, UpdateEscalationRuleRequest request) {
        log.info("Updating escalation rule with ID: {}", id);

        EscalationRule escalationRule = escalationRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Escalation rule not found"));

        escalationRule.setName(request.getName());
        escalationRule.setDescription(request.getDescription());
        if (request.getTriggerType() != null) {
            escalationRule.setTriggerType(request.getTriggerType());
        }
        escalationRule.setTriggerValue(request.getTriggerValue());
        escalationRule.setThresholdValue(request.getThresholdValue());
        escalationRule.setTimeThresholdMinutes(request.getTimeThresholdMinutes());
        escalationRule.setConditions(request.getConditions());
        if (request.getEscalationAction() != null) {
            escalationRule.setEscalationAction(request.getEscalationAction());
        }
        escalationRule.setEscalateToAgentId(request.getEscalateToAgentId());
        escalationRule.setEscalateToTeamId(request.getEscalateToTeamId());
        if (request.getEscalateToSupervisor() != null) {
            escalationRule.setEscalateToSupervisor(request.getEscalateToSupervisor());
        }
        if (request.getNotifySupervisor() != null) {
            escalationRule.setNotifySupervisor(request.getNotifySupervisor());
        }
        if (request.getPriority() != null) {
            escalationRule.setPriority(request.getPriority());
        }
        if (request.getIsActive() != null) {
            escalationRule.setIsActive(request.getIsActive());
        }

        EscalationRule updated = escalationRuleRepository.save(escalationRule);
        log.info("Escalation rule updated successfully");
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteEscalationRule(Long id) {
        log.info("Deleting escalation rule with ID: {}", id);
        EscalationRule escalationRule = escalationRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Escalation rule not found"));
        escalationRuleRepository.delete(escalationRule);
        log.info("Escalation rule deleted successfully");
    }

    @Transactional(readOnly = true)
    public EscalationRuleDTO getEscalationRuleById(Long id) {
        EscalationRule escalationRule = escalationRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Escalation rule not found"));
        return mapToDTO(escalationRule);
    }

    @Transactional(readOnly = true)
    public List<EscalationRuleDTO> getAllEscalationRules() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<EscalationRule> rules = escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(tenantId);
        return rules.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EscalationRuleDTO> getEscalationRulesByTriggerType(EscalationRule.TriggerType triggerType) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<EscalationRule> rules = escalationRuleRepository.findByTenantIdAndTriggerTypeAndIsActiveTrue(tenantId, triggerType);
        return rules.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EscalationRuleDTO> getEscalationRulesByAction(EscalationRule.EscalationAction action) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<EscalationRule> rules = escalationRuleRepository.findByTenantIdAndEscalationActionAndIsActiveTrue(tenantId, action);
        return rules.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private EscalationRuleDTO mapToDTO(EscalationRule escalationRule) {
        return EscalationRuleDTO.builder()
                .id((Long) escalationRule.getId())
                .name(escalationRule.getName())
                .description(escalationRule.getDescription())
                .triggerType(escalationRule.getTriggerType())
                .triggerValue(escalationRule.getTriggerValue())
                .thresholdValue(escalationRule.getThresholdValue())
                .timeThresholdMinutes(escalationRule.getTimeThresholdMinutes())
                .conditions(escalationRule.getConditions())
                .escalationAction(escalationRule.getEscalationAction())
                .escalateToAgentId(escalationRule.getEscalateToAgentId())
                .escalateToTeamId(escalationRule.getEscalateToTeamId())
                .escalateToSupervisor(escalationRule.getEscalateToSupervisor())
                .notifySupervisor(escalationRule.getNotifySupervisor())
                .priority(escalationRule.getPriority())
                .isActive(escalationRule.getIsActive())
                .createdAt(escalationRule.getCreatedAt())
                .updatedAt(escalationRule.getUpdatedAt())
                .build();
    }

    @Transactional
    public EscalationResult evaluateEscalation(Long conversationId, EscalationContext context) {
        log.info("Evaluating escalation for conversation {} with context: {}", conversationId, context);
        
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        
        // Get all active escalation rules ordered by priority
        List<EscalationRule> rules = escalationRuleRepository.findByTenantIdAndIsActiveTrueOrderByPriorityAsc(tenantId);
        
        if (rules.isEmpty()) {
            log.warn("No active escalation rules found for tenant {}", tenantId);
            return EscalationResult.builder()
                    .conversationId(conversationId)
                    .escalated(false)
                    .reason("No active escalation rules")
                    .build();
        }
        
        // Evaluate rules in priority order
        for (EscalationRule rule : rules) {
            if (shouldEscalate(rule, context)) {
                return executeEscalation(rule, conversationId, context);
            }
        }
        
        return EscalationResult.builder()
                .conversationId(conversationId)
                .escalated(false)
                .reason("No escalation rules matched")
                .build();
    }
    
    private boolean shouldEscalate(EscalationRule rule, EscalationContext context) {
        log.info("Checking escalation rule {} for trigger type {}", rule.getName(), rule.getTriggerType());
        
        switch (rule.getTriggerType()) {
            case WAIT_TIME_EXCEEDED:
                return checkWaitTimeExceeded(rule, context);
            case NO_RESPONSE:
                return checkNoResponse(rule, context);
            case LOW_CUSTOMER_SATISFACTION:
                return checkLowCustomerSatisfaction(rule, context);
            case HIGH_PRIORITY_CUSTOMER:
                return checkHighPriorityCustomer(rule, context);
            case CUSTOMER_TIER:
                return checkCustomerTier(rule, context);
            case CUSTOM_CONDITION:
                return checkCustomCondition(rule, context);
            default:
                return false;
        }
    }
    
    private boolean checkWaitTimeExceeded(EscalationRule rule, EscalationContext context) {
        if (context.getWaitTimeMinutes() == null) {
            return false;
        }
        
        Integer threshold = rule.getTimeThresholdMinutes();
        if (threshold == null) {
            threshold = rule.getThresholdValue();
        }
        
        return context.getWaitTimeMinutes() >= threshold;
    }
    
    private boolean checkNoResponse(EscalationRule rule, EscalationContext context) {
        if (context.getLastResponseTime() == null) {
            return false;
        }
        
        long minutesSinceResponse = ChronoUnit.MINUTES.between(context.getLastResponseTime(), LocalDateTime.now());
        Integer threshold = rule.getTimeThresholdMinutes();
        if (threshold == null) {
            threshold = rule.getThresholdValue();
        }
        
        return minutesSinceResponse >= threshold;
    }
    
    private boolean checkLowCustomerSatisfaction(EscalationRule rule, EscalationContext context) {
        if (context.getCustomerSatisfactionScore() == null) {
            return false;
        }
        
        Integer threshold = rule.getThresholdValue();
        return context.getCustomerSatisfactionScore() <= threshold;
    }
    
    private boolean checkHighPriorityCustomer(EscalationRule rule, EscalationContext context) {
        if (context.getCustomerPriority() == null) {
            return false;
        }
        
        String expectedPriority = rule.getTriggerValue();
        return expectedPriority != null && expectedPriority.equalsIgnoreCase(context.getCustomerPriority());
    }
    
    private boolean checkCustomerTier(EscalationRule rule, EscalationContext context) {
        if (context.getCustomerTier() == null) {
            return false;
        }
        
        String expectedTier = rule.getTriggerValue();
        return expectedTier != null && expectedTier.equalsIgnoreCase(context.getCustomerTier());
    }
    
    private boolean checkCustomCondition(EscalationRule rule, EscalationContext context) {
        if (rule.getConditions() == null || rule.getConditions().isEmpty()) {
            return false;
        }
        
        // Evaluate custom conditions
        for (Map.Entry<String, Object> entry : rule.getConditions().entrySet()) {
            String key = entry.getKey();
            Object expectedValue = entry.getValue();
            Object actualValue = context.getCustomData().get(key);
            
            if (!Objects.equals(expectedValue, actualValue)) {
                return false;
            }
        }
        
        return true;
    }
    
    private EscalationResult executeEscalation(EscalationRule rule, Long conversationId, EscalationContext context) {
        log.info("Executing escalation rule {} for conversation {}", rule.getName(), conversationId);
        
        Long escalatedToAgentId = null;
        Long escalatedToTeamId = null;
        boolean escalatedToSupervisor = rule.getEscalateToSupervisor();
        String escalationAction = rule.getEscalationAction().name();
        
        switch (rule.getEscalationAction()) {
            case ASSIGN_TO_AGENT:
                escalatedToAgentId = rule.getEscalateToAgentId();
                break;
            case ASSIGN_TO_TEAM:
                escalatedToTeamId = rule.getEscalateToTeamId();
                break;
            case ASSIGN_TO_SUPERVISOR:
                escalatedToSupervisor = true;
                break;
            case MOVE_TO_QUEUE:
                // Move to queue logic
                log.info("Moving conversation {} to queue", conversationId);
                break;
            case SEND_NOTIFICATION:
                // Send notification logic
                log.info("Sending notification for conversation {}", conversationId);
                break;
        }
        
        // Notify supervisor if configured
        if (rule.getNotifySupervisor()) {
            log.info("Notifying supervisor about escalation of conversation {}", conversationId);
            // Implement notification logic
        }
        
        return EscalationResult.builder()
                .conversationId(conversationId)
                .escalated(true)
                .escalatedToAgentId(escalatedToAgentId)
                .escalatedToTeamId(escalatedToTeamId)
                .escalatedToSupervisor(escalatedToSupervisor)
                .escalationRuleId((Long) rule.getId())
                .escalationAction(escalationAction)
                .escalatedAt(LocalDateTime.now())
                .reason("Escalation rule matched: " + rule.getName())
                .build();
    }
    
    @Transactional(readOnly = true)
    public List<EscalationResult> getEscalationHistory(Long conversationId) {
        // Placeholder for retrieving escalation history
        log.info("Retrieving escalation history for conversation {}", conversationId);
        return Collections.emptyList();
    }
}
