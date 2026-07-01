package com.chatbot.core.message.store.repository;

import com.chatbot.core.message.store.model.EscalationTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EscalationTierRepository extends JpaRepository<EscalationTier, Long> {

    /**
     * Find all escalation tiers by tenant ID
     */
    List<EscalationTier> findByTenantId(Long tenantId);

    /**
     * Find active escalation tiers by tenant ID, ordered by level
     */
    @Query("SELECT et FROM EscalationTier et WHERE et.tenantId = :tenantId AND et.active = true ORDER BY et.level ASC")
    List<EscalationTier> findByTenantIdAndActiveOrderByLevelAsc(
        @Param("tenantId") Long tenantId,
        @Param("active") Boolean active
    );

    /**
     * Find escalation tier by tenant ID and level
     */
    EscalationTier findByTenantIdAndLevel(@Param("tenantId") Long tenantId, @Param("level") Integer level);

    /**
     * Count escalation tiers by tenant ID
     */
    Long countByTenantId(Long tenantId);
}
