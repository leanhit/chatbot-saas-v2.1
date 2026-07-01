package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.notification.websocket.NotificationWebSocketHandler;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for handling timeout scenarios in conversations
 * Implements Phase 2.3: Special Workflows - TimeoutWorkflow
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimeoutWorkflow {

    private final ConversationRepository conversationRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final TenantRepository tenantRepository;
    private final ConversationEndWorkflow conversationEndWorkflow;

    // Timeout thresholds (in minutes)
    private static final long INACTIVE_TIMEOUT = 30; // 30 minutes of inactivity
    private static final long UNRESPONDED_TIMEOUT = 60; // 60 minutes without agent response

    /**
     * Check for inactive conversations
     * Scheduled to run every 5 minutes
     * Loops through all active tenants to check for inactive conversations
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void checkInactiveConversations() {
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
                        if (isConversationInactive(conversation)) {
                            handleInactiveConversation(conversation);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error checking inactive conversations for tenant {}", tenantId, e);
                }
            }
        } catch (Exception e) {
            log.error("Error in inactive conversation check scheduled job", e);
        }
    }

    /**
     * Check if conversation is inactive
     */
    public boolean isConversationInactive(Conversation conversation) {
        LocalDateTime lastActivity = conversation.getUpdatedAt();
        if (lastActivity == null) {
            lastActivity = conversation.getCreatedAt();
        }

        long minutesSinceLastActivity = ChronoUnit.MINUTES.between(lastActivity, LocalDateTime.now());
        return minutesSinceLastActivity >= INACTIVE_TIMEOUT;
    }

    /**
     * Handle inactive conversation
     */
    private void handleInactiveConversation(Conversation conversation) {
        log.info("Handling inactive conversation {}", conversation.getId());

        // Send timeout message to user
        sendTimeoutMessage(conversation);

        // Notify agents
        notifyAgentsAboutTimeout(conversation);

        // Optionally auto-close or reassign based on configuration
        // For now, we'll just notify
    }

    /**
     * Send timeout message to user
     */
    private void sendTimeoutMessage(Conversation conversation) {
        String timeoutMessage = "It looks like our conversation has been inactive for a while. " +
                               "If you still need assistance, please send us a message and we'll be happy to help.";

        log.info("Sending timeout message to user in conversation {}: {}",
            conversation.getId(), timeoutMessage);

        // TODO: Integrate with channel-specific message sending (Facebook, Zalo, etc.)
    }

    /**
     * Notify agents about timeout
     */
    private void notifyAgentsAboutTimeout(Conversation conversation) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "conversation_timeout");
        notification.put("title", "Conversation Timeout");
        notification.put("message", String.format(
            "Conversation %d has been inactive for %d minutes. Customer: %s",
            conversation.getId(),
            INACTIVE_TIMEOUT,
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId()
        ));
        notification.put("conversationId", conversation.getId());
        notification.put("priority", "medium");
        notification.put("timestamp", LocalDateTime.now());

        notificationWebSocketHandler.broadcastToTenant(conversation.getTenantId(), notification);

        log.info("Notified agents about timeout in conversation {}", conversation.getId());
    }

    /**
     * Check for conversations without agent response
     * Scheduled to run every 10 minutes
     * Loops through all active tenants to check for unresponded conversations
     */
    @Scheduled(fixedRate = 600000) // Every 10 minutes
    public void checkUnrespondedConversations() {
        try {
            // Get all active tenants
            List<Tenant> activeTenants = tenantRepository.findAll();
            
            for (Tenant tenant : activeTenants) {
                if (tenant.getStatus() != TenantStatus.ACTIVE) {
                    continue; // Skip inactive tenants
                }
                
                Long tenantId = tenant.getId();
                
                try {
                    // Get all open conversations taken over by agents for this tenant
                    List<Conversation> agentConversations = conversationRepository.findByIsTakenOverByAgentAndTenantId(true, tenantId);

                    for (Conversation conversation : agentConversations) {
                        if (isConversationUnresponded(conversation)) {
                            handleUnrespondedConversation(conversation);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error checking unresponded conversations for tenant {}", tenantId, e);
                }
            }
        } catch (Exception e) {
            log.error("Error in unresponded conversation check scheduled job", e);
        }
    }

    /**
     * Check if conversation is unresponded (agent took over but hasn't responded)
     */
    public boolean isConversationUnresponded(Conversation conversation) {
        // If agent took over but no agent response time recorded
        if (conversation.getIsTakenOverByAgent() && conversation.getFirstAgentResponseTime() == null) {
            LocalDateTime takeoverTime = conversation.getUpdatedAt();
            long minutesSinceTakeover = ChronoUnit.MINUTES.between(takeoverTime, LocalDateTime.now());
            return minutesSinceTakeover >= UNRESPONDED_TIMEOUT;
        }
        return false;
    }

    /**
     * Handle unresponded conversation
     */
    private void handleUnrespondedConversation(Conversation conversation) {
        log.warn("Handling unresponded conversation {}", conversation.getId());

        // Send urgent notification to agents
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "unresponded_conversation");
        notification.put("title", "Unresponded Conversation Alert");
        notification.put("message", String.format(
            "Conversation %d was taken over by an agent %d minutes ago but no response has been sent. Customer: %s",
            conversation.getId(),
            UNRESPONDED_TIMEOUT,
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId()
        ));
        notification.put("conversationId", conversation.getId());
        notification.put("priority", "high");
        notification.put("timestamp", LocalDateTime.now());

        notificationWebSocketHandler.broadcastToTenant(conversation.getTenantId(), notification);

        // Consider auto-releasing the conversation back to bot
        // This could be configurable
        log.warn("Consider auto-releasing conversation {} back to bot", conversation.getId());
    }

    /**
     * Auto-close inactive conversations
     * This can be called manually or scheduled
     */
    @Transactional
    public void autoCloseInactiveConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            return;
        }

        log.info("Auto-closing inactive conversation {}", conversationId);
        conversationEndWorkflow.handleConversationEnd(conversationId, "timeout");
    }

    /**
     * Reassign conversation to bot
     */
    @Transactional
    public void reassignToBot(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            return;
        }

        log.info("Reassigning conversation {} back to bot", conversationId);

        conversation.setIsTakenOverByAgent(false);
        conversation.setAgentAssignedId(null);
        conversationRepository.save(conversation);

        // Notify agents
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "conversation_reassigned");
        notification.put("title", "Conversation Reassigned to Bot");
        notification.put("message", String.format(
            "Conversation %d has been reassigned to bot. Customer: %s",
            conversation.getId(),
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId()
        ));
        notification.put("conversationId", conversation.getId());
        notification.put("priority", "medium");
        notification.put("timestamp", LocalDateTime.now());

        notificationWebSocketHandler.broadcastToTenant(conversation.getTenantId(), notification);
    }
}
