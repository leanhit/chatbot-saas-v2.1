package com.chatbot.core.routing.repository;

import com.chatbot.core.routing.model.EscalationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntelligentEscalationRuleRepository extends JpaRepository<EscalationRule, Long> {

    List<EscalationRule> findByTenantIdAndIsActiveTrueOrderByPriorityAsc(Long tenantId);

    List<EscalationRule> findByTenantIdAndTriggerTypeAndIsActiveTrue(Long tenantId, EscalationRule.TriggerType triggerType);

    Optional<EscalationRule> findFirstByTenantIdAndIsActiveTrueOrderByPriorityAsc(Long tenantId);

    List<EscalationRule> findByTenantIdAndEscalationActionAndIsActiveTrue(Long tenantId, EscalationRule.EscalationAction action);
}
