package com.chatbot.core.routing.service;

import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.model.EscalationRule;
import com.chatbot.core.routing.repository.IntelligentEscalationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
}
