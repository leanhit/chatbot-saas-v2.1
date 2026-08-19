package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.EscalationTier;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.EscalationTierRepository;
import com.chatbot.core.notification.websocket.NotificationWebSocketHandler;
import com.chatbot.core.notification.email.AgentEmailNotificationService;
import com.chatbot.core.notification.slack.SlackNotificationService;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing multi-tier escalation
 * Implements Phase 2.2: Multi-tier Escalation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EscalationService {

    private final ConversationRepository conversationRepository;
    private final EscalationTierRepository escalationTierRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final TenantRepository tenantRepository;
    private final AgentEmailNotificationService agentEmailNotificationService;
    private final SlackNotificationService slackNotificationService;

    /**
     * Check for conversations that need escalation
     * Scheduled to run every minute
     * Loops through all active tenants to check for escalations
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @SchedulerLock(name = "EscalationService_checkEscalations", lockAtMostFor = "2m", lockAtLeastFor = "1m")
    public void checkEscalations() {
        try {
            // Get all active tenants
            List<Tenant> activeTenants = tenantRepository.findAll();
            
            for (Tenant tenant : activeTenants) {
                if (tenant.getStatus() != TenantStatus.ACTIVE) {
                    continue; // Skip inactive tenants
                }
                
                Long tenantId = tenant.getId();
                
                try {
                    // Get all open conversations for this tenant
                    List<Conversation> openConversations = conversationRepository.findByTenantIdAndStatus(tenantId, "open");

                    for (Conversation conversation : openConversations) {
                        if (shouldEscalate(conversation)) {
                            escalateConversation(conversation);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error checking escalations for tenant {}", tenantId, e);
                }
            }
        } catch (Exception e) {
            log.error("Error in escalation check scheduled job", e);
        }
    }

    /**
     * Determine if a conversation should be escalated
     * Skips if already taken over by agent or if already at highest tier
     */
    public boolean shouldEscalate(Conversation conversation) {
        // Skip if already taken over by agent
        if (conversation.getIsTakenOverByAgent()) {
            return false;
        }

        // Get escalation tiers for tenant
        List<EscalationTier> tiers = escalationTierRepository.findByTenantIdAndActiveOrderByLevelAsc(
            conversation.getTenantId(), true
        );

        if (tiers.isEmpty()) {
            return false;
        }

        // Determine the tier the conversation should be at based on elapsed time
        LocalDateTime createdAt = conversation.getCreatedAt();
        long secondsSinceCreation = ChronoUnit.SECONDS.between(createdAt, LocalDateTime.now());

        EscalationTier targetTier = null;
        for (EscalationTier tier : tiers) {
            if (secondsSinceCreation >= tier.getTimeoutSeconds()) {
                targetTier = tier;
            }
        }

        if (targetTier == null) {
            return false; // Not yet time for any escalation
        }

        // Skip if already escalated to this tier or higher
        Integer currentTier = conversation.getCurrentEscalationTier();
        if (currentTier != null && currentTier >= targetTier.getLevel()) {
            return false;
        }

        return true;
    }

    /**
     * Escalate a conversation to the appropriate tier based on elapsed time
     * Also persists escalation tracking fields to the Conversation entity
     */
    @Transactional
    public void escalateConversation(Conversation conversation) {
        log.info("Escalating conversation {} to next tier", conversation.getId());

        // Get escalation tiers for tenant
        List<EscalationTier> tiers = escalationTierRepository.findByTenantIdAndActiveOrderByLevelAsc(
            conversation.getTenantId(), true
        );

        if (tiers.isEmpty()) {
            log.warn("No escalation tiers configured for tenant {}", conversation.getTenantId());
            return;
        }

        // Determine current tier based on time elapsed
        LocalDateTime createdAt = conversation.getCreatedAt();
        long secondsSinceCreation = ChronoUnit.SECONDS.between(createdAt, LocalDateTime.now());

        EscalationTier targetTier = null;
        for (EscalationTier tier : tiers) {
            if (secondsSinceCreation >= tier.getTimeoutSeconds()) {
                targetTier = tier;
            }
        }

        if (targetTier != null) {
            // Persist escalation tracking to Conversation entity
            conversation.setCurrentEscalationTier(targetTier.getLevel());
            conversation.setLastEscalatedAt(LocalDateTime.now());
            conversationRepository.save(conversation);
            log.info("Updated escalation tracking for conversation {}: tier={}",
                conversation.getId(), targetTier.getLevel());

            sendEscalationNotification(conversation, targetTier);
        }
    }

    /**
     * Escalate a conversation to a specific tier level or name
     * Also persists escalation tracking fields to the Conversation entity
     */
    @Transactional
    public void escalateConversationToTier(Conversation conversation, String tierNameOrLevel) {
        log.info("Escalating conversation {} to tier {}", conversation.getId(), tierNameOrLevel);
        
        List<EscalationTier> tiers = escalationTierRepository.findByTenantIdAndActiveOrderByLevelAsc(
            conversation.getTenantId(), true
        );
        
        EscalationTier targetTier = null;
        try {
            int level = Integer.parseInt(tierNameOrLevel);
            targetTier = tiers.stream().filter(t -> t.getLevel() == level).findFirst().orElse(null);
        } catch (NumberFormatException e) {
            targetTier = tiers.stream().filter(t -> t.getName().equalsIgnoreCase(tierNameOrLevel)).findFirst().orElse(null);
        }
        
        if (targetTier != null) {
            // Persist escalation tracking
            conversation.setCurrentEscalationTier(targetTier.getLevel());
            conversation.setLastEscalatedAt(LocalDateTime.now());
            conversationRepository.save(conversation);
            sendEscalationNotification(conversation, targetTier);
        } else {
            log.warn("Escalation tier {} not found for tenant {}", tierNameOrLevel, conversation.getTenantId());
        }
    }

    /**
     * Send escalation notification via WebSocket, email, and Slack
     */
    private void sendEscalationNotification(Conversation conversation, EscalationTier tier) {
        String message = String.format(
            "Conversation %d has been escalated to Tier %d (%s). Customer: %s",
            conversation.getId(),
            tier.getLevel(),
            tier.getName(),
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId()
        );

        // 1. WebSocket broadcast to tenant
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "escalation");
        notification.put("title", "Conversation Escalation");
        notification.put("message", message);
        notification.put("conversationId", conversation.getId());
        notification.put("escalationTier", tier.getLevel());
        notification.put("escalationTierName", tier.getName());
        notification.put("priority", "high");
        notification.put("timestamp", LocalDateTime.now());
        notificationWebSocketHandler.broadcastToTenant(conversation.getTenantId(), notification);

        // 2. Email notification (non-blocking, graceful fail)
        try {
            agentEmailNotificationService.sendEscalationNotification(conversation, tier.getLevel(), tier.getName());
        } catch (Exception e) {
            log.warn("Failed to send escalation email for conversation {}: {}", conversation.getId(), e.getMessage());
        }

        // 3. Slack notification (non-blocking, graceful fail)
        try {
            slackNotificationService.sendEscalationAlert(conversation, tier.getLevel(), tier.getName());
        } catch (Exception e) {
            log.warn("Failed to send escalation Slack alert for conversation {}: {}", conversation.getId(), e.getMessage());
        }

        log.info("Sent escalation notification for conversation {} to tier {}", conversation.getId(), tier.getLevel());
    }

    /**
     * Create default escalation tiers for a tenant
     */
    @Transactional
    public void createDefaultEscalationTiers(Long tenantId) {
        if (escalationTierRepository.findByTenantId(tenantId).isEmpty()) {
            // Tier 1: Agent (5 minutes)
            EscalationTier tier1 = EscalationTier.builder()
                .tenantId(tenantId)
                .level(1)
                .name("Agent")
                .timeoutSeconds(300L) // 5 minutes
                .active(true)
                .description("First line support agents")
                .requiredRole("AGENT")
                .build();

            // Tier 2: Team Lead (15 minutes)
            EscalationTier tier2 = EscalationTier.builder()
                .tenantId(tenantId)
                .level(2)
                .name("Team Lead")
                .timeoutSeconds(900L) // 15 minutes
                .active(true)
                .description("Team leads for complex issues")
                .requiredRole("TEAM_LEAD")
                .build();

            // Tier 3: Supervisor (30 minutes)
            EscalationTier tier3 = EscalationTier.builder()
                .tenantId(tenantId)
                .level(3)
                .name("Supervisor")
                .timeoutSeconds(1800L) // 30 minutes
                .active(true)
                .description("Supervisors for critical issues")
                .requiredRole("SUPERVISOR")
                .build();

            escalationTierRepository.save(tier1);
            escalationTierRepository.save(tier2);
            escalationTierRepository.save(tier3);

            log.info("Created default escalation tiers for tenant {}", tenantId);
        }
    }

    /**
     * Get escalation tiers for a tenant
     */
    public List<EscalationTier> getEscalationTiers(Long tenantId) {
        return escalationTierRepository.findByTenantIdAndActiveOrderByLevelAsc(tenantId, true);
    }

    /**
     * Update escalation tier
     */
    @Transactional
    public EscalationTier updateEscalationTier(Long tierId, EscalationTier updatedTier) {
        return escalationTierRepository.findById(tierId)
            .map(existingTier -> {
                existingTier.setName(updatedTier.getName());
                existingTier.setTimeoutSeconds(updatedTier.getTimeoutSeconds());
                existingTier.setActive(updatedTier.getActive());
                existingTier.setDescription(updatedTier.getDescription());
                existingTier.setRequiredRole(updatedTier.getRequiredRole());
                return escalationTierRepository.save(existingTier);
            })
            .orElseThrow(() -> new RuntimeException("Escalation tier not found: " + tierId));
    }

    /**
     * Delete escalation tier
     */
    @Transactional
    public void deleteEscalationTier(Long tierId) {
        escalationTierRepository.deleteById(tierId);
    }
}
