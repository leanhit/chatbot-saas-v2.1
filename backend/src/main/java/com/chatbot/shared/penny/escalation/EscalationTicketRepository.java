package com.chatbot.shared.penny.escalation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for EscalationTicket — supports tenant-scoped queries
 * and filtering by status, priority, and assigned agent.
 */
@Repository
public interface EscalationTicketRepository extends JpaRepository<EscalationTicket, UUID> {

    /**
     * Find all tickets for a bot, tenant-scoped, pageable.
     */
    Page<EscalationTicket> findByBotIdAndTenantId(UUID botId, Long tenantId, Pageable pageable);

    /**
     * Find tickets by status for a bot, tenant-scoped.
     */
    List<EscalationTicket> findByBotIdAndTenantIdAndStatus(UUID botId, Long tenantId, String status);

    /**
     * Find tickets by status and priority for a bot, tenant-scoped.
     */
    List<EscalationTicket> findByBotIdAndTenantIdAndStatusAndPriority(
        UUID botId, Long tenantId, String status, String priority);

    /**
     * Find tickets assigned to a specific agent.
     */
    List<EscalationTicket> findByAssignedAgentId(String agentId);

    /**
     * Find pending tickets for a bot (tenant-scoped).
     */
    @Query("SELECT t FROM EscalationTicket t " +
           "WHERE t.botId = :botId AND t.tenantId = :tenantId AND t.status = 'PENDING' " +
           "ORDER BY t.priority DESC, t.createdAt ASC")
    List<EscalationTicket> findPendingTickets(
        @Param("botId") UUID botId,
        @Param("tenantId") Long tenantId);

    /**
     * Count tickets by status for a bot, tenant-scoped.
     */
    long countByBotIdAndTenantIdAndStatus(UUID botId, Long tenantId, String status);

    /**
     * Count all tickets for a bot, tenant-scoped.
     */
    long countByBotIdAndTenantId(UUID botId, Long tenantId);

    /**
     * Find tickets by user ID.
     */
    List<EscalationTicket> findByUserId(String userId);

    /**
     * Find tickets by user ID and status.
     */
    List<EscalationTicket> findByUserIdAndStatus(String userId, String status);
}
