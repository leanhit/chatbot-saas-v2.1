package com.chatbot.core.message.store.service;

import com.chatbot.core.membership.event.MemberActivatedEvent;
import com.chatbot.core.membership.event.MemberDeactivatedEvent;
import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service responsible for automatically provisioning and deactivating Agent profiles
 * in response to TenantMember lifecycle events.
 *
 * Called by MemberEventListener after transaction commits.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgentProvisioningService {

    private final AgentRepository agentRepository;

    /**
     * Provision an Agent profile for a newly activated TenantMember.
     * If an Agent already exists (e.g., re-join scenario), reactivate it instead.
     */
    @Transactional(transactionManager = "messageTransactionManager")
    public void provisionAgent(MemberActivatedEvent event) {
        log.info("[AgentProvisioning] Provisioning agent for userId={} in tenantId={}",
                event.getUserId(), event.getTenantId());

        agentRepository.findByUserIdAndTenantId(event.getUserId(), event.getTenantId())
                .ifPresentOrElse(
                        existing -> reactivateAgent(existing),
                        () -> createDefaultAgent(event)
                );
    }

    /**
     * Repair a missing Agent profile for an ACTIVE TenantMember.
     * Idempotent: safe to call multiple times. If Agent already exists, reactivates it.
     * Called manually via POST /api/agents/repair/{userId} when auto-provisioning failed.
     */
    @Transactional(transactionManager = "messageTransactionManager")
    public void repairAgentForMember(Long tenantId, Long userId,
                                     String userEmail, String userName, String userAvatar) {
        log.info("[AgentProvisioning] Repair requested for userId={} in tenantId={}", userId, tenantId);

        MemberActivatedEvent event = MemberActivatedEvent.builder()
                .tenantId(tenantId)
                .userId(userId)
                .userEmail(userEmail)
                .userName(userName)
                .userAvatar(userAvatar)
                .build();

        agentRepository.findByUserIdAndTenantId(userId, tenantId)
                .ifPresentOrElse(
                        existing -> {
                            reactivateAgent(existing);
                            log.info("[AgentProvisioning] Repair: reactivated existing agent id={}", existing.getId());
                        },
                        () -> {
                            createDefaultAgent(event);
                            log.info("[AgentProvisioning] Repair: created new agent for userId={} in tenantId={}", userId, tenantId);
                        }
                );
    }

    /**
     * Deactivate Agent profile when a TenantMember is removed or blocked.
     * Sets agent to inactive + OFFLINE — does NOT hard-delete to preserve history.
     */
    @Transactional(transactionManager = "messageTransactionManager")
    public void deactivateAgent(Long tenantId, Long userId) {
        agentRepository.findByUserIdAndTenantId(userId, tenantId)
                .ifPresent(agent -> {
                    agent.setActive(false);
                    agent.setStatus(Agent.AgentStatus.OFFLINE);
                    agent.setUpdatedAt(LocalDateTime.now());
                    agentRepository.save(agent);
                    log.info("[AgentProvisioning] Deactivated agent id={} for userId={} in tenantId={}",
                            agent.getId(), userId, tenantId);
                });
    }

    /* ============ Private Helpers ============ */

    private void createDefaultAgent(MemberActivatedEvent event) {
        Agent agent = Agent.builder()
                .userId(event.getUserId())
                .tenantId(event.getTenantId())
                .name(event.getUserName() != null ? event.getUserName() : event.getUserEmail())
                .email(event.getUserEmail())
                .role(Agent.AgentRole.AGENT)
                .status(Agent.AgentStatus.OFFLINE)
                .maxConcurrentConversations(5)
                .currentLoad(0)
                .active(true)
                .avatarUrl(event.getUserAvatar())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastActivityAt(LocalDateTime.now())
                .build();

        agentRepository.save(agent);
        log.info("[AgentProvisioning] Created new agent for userId={} in tenantId={}, email={}",
                event.getUserId(), event.getTenantId(), event.getUserEmail());
    }

    private void reactivateAgent(Agent existing) {
        existing.setActive(true);
        existing.setUpdatedAt(LocalDateTime.now());
        agentRepository.save(existing);
        log.info("[AgentProvisioning] Reactivated existing agent id={} for userId={} in tenantId={}",
                existing.getId(), existing.getUserId(), existing.getTenantId());
    }
}
