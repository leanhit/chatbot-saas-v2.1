package com.chatbot.core.routing.service;

import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.model.RoutingRule;
import com.chatbot.core.routing.repository.IntelligentRoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
}
