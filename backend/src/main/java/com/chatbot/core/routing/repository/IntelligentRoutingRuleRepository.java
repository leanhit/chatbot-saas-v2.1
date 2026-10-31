package com.chatbot.core.routing.repository;

import com.chatbot.core.routing.model.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntelligentRoutingRuleRepository extends JpaRepository<RoutingRule, Long> {

    List<RoutingRule> findByTenantIdAndIsActiveTrueOrderByPriorityAsc(Long tenantId);

    List<RoutingRule> findByTenantIdAndIsActiveTrueAndIsDefaultTrue(Long tenantId);

    Optional<RoutingRule> findFirstByTenantIdAndIsActiveTrueOrderByPriorityAsc(Long tenantId);

    List<RoutingRule> findByTenantIdAndRuleTypeAndIsActiveTrue(Long tenantId, RoutingRule.RoutingRuleType ruleType);

    List<RoutingRule> findByTenantIdAndTargetTypeAndIsActiveTrue(Long tenantId, RoutingRule.TargetType targetType);
}
