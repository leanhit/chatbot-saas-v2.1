package com.chatbot.core.message.store.repository;

import com.chatbot.core.message.store.model.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    /**
     * Find all agents by tenant ID
     */
    List<Agent> findByTenantId(Long tenantId);

    /**
     * Find active agents by tenant ID
     */
    List<Agent> findByTenantIdAndActiveOrderByCreatedAtDesc(
        @Param("tenantId") Long tenantId,
        @Param("active") Boolean active
    );

    /**
     * Find agents by tenant ID and status
     */
    List<Agent> findByTenantIdAndStatus(
        @Param("tenantId") Long tenantId,
        @Param("status") Agent.AgentStatus status
    );

    /**
     * Find agents by tenant ID and role
     */
    List<Agent> findByTenantIdAndRole(
        @Param("tenantId") Long tenantId,
        @Param("role") Agent.AgentRole role
    );

    /**
     * Find online agents by tenant ID who can accept more conversations
     */
    @Query("SELECT a FROM Agent a WHERE a.tenantId = :tenantId AND a.status = 'ONLINE' AND a.active = true AND a.currentLoad < a.maxConcurrentConversations")
    List<Agent> findAvailableAgentsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Find agent by user ID
     */
    Optional<Agent> findByUserId(Long userId);

    /**
     * Find agent by email
     */
    Optional<Agent> findByEmail(String email);

    /**
     * Count agents by tenant ID
     */
    Long countByTenantId(Long tenantId);

    /**
     * Count active agents by tenant ID
     */
    Long countByTenantIdAndActive(@Param("tenantId") Long tenantId, @Param("active") Boolean active);

    /**
     * Count online agents by tenant ID
     */
    Long countByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") Agent.AgentStatus status);

    /**
     * Find agents by tenant ID and skill using JSON query
     * This avoids N+1 query by filtering at database level
     */
    @Query(value = "SELECT * FROM agents WHERE tenant_id = :tenantId AND jsonb_exists(skills, :skill)", nativeQuery = true)
    List<Agent> findAgentsBySkill(@Param("tenantId") Long tenantId, @Param("skill") String skill);

    /**
     * Find available agents by tenant ID and skill using JSON query
     */
    @Query(value = "SELECT * FROM agents WHERE tenant_id = :tenantId AND jsonb_exists(skills, :skill) AND status = 'ONLINE' AND active = true AND current_load < max_concurrent_conversations", nativeQuery = true)
    List<Agent> findAvailableAgentsBySkill(@Param("tenantId") Long tenantId, @Param("skill") String skill);
}
