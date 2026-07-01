package com.chatbot.core.message.store.repository;

import com.chatbot.core.message.store.model.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutingRuleRepository extends JpaRepository<RoutingRule, Long> {

    /**
     * Find all active routing rules for a tenant ordered by priority (descending)
     */
    List<RoutingRule> findByTenantIdAndActiveOrderByPriorityDesc(
        @Param("tenantId") Long tenantId,
        @Param("active") Boolean active
    );

    /**
     * Find all routing rules for a tenant
     */
    List<RoutingRule> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Find routing rules by type for a tenant
     */
    List<RoutingRule> findByTenantIdAndRuleTypeAndActiveOrderByPriorityDesc(
        @Param("tenantId") Long tenantId,
        @Param("ruleType") RoutingRule.RoutingRuleType ruleType,
        @Param("active") Boolean active
    );
}
