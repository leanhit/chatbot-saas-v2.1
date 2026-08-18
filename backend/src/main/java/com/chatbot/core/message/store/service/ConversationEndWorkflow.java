package com.chatbot.core.message.store.service;

import com.chatbot.core.message.decision.exception.ConversationNotFoundException;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.notification.websocket.NotificationWebSocketHandler;
import com.chatbot.shared.messenger.ChannelMessengerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling conversation end scenarios
 * Implements Phase 2.3: Special Workflows - ConversationEndWorkflow
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationEndWorkflow {

    private final ConversationRepository conversationRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final ChannelMessengerService channelMessengerService;

    /**
     * Handle conversation end
     * @param conversationId The conversation ID
     * @param endReason Reason for conversation end (user_closed, agent_closed, auto_closed, timeout)
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public void handleConversationEnd(Long conversationId, String endReason) {
        log.info("Handling conversation end for {} - Reason: {}", conversationId, endReason);

        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found for end workflow: " + conversationId));

        // Close the conversation
        closeConversation(conversation, endReason);

        // Summarize conversation
        summarizeConversation(conversation);

        // Offer follow-up actions
        offerFollowUpActions(conversation);

        // Update customer data if applicable
        updateCustomerData(conversation);

        // Notify agents
        notifyAgentsConversationEnded(conversation, endReason);
    }

    /**
     * Close conversation
     */
    private void closeConversation(Conversation conversation, String endReason) {
        conversation.setStatus("closed");
        if ("agent_closed".equals(endReason)) {
            conversation.setIsClosedByAgent(true);
        }
        conversation.setIsTakenOverByAgent(false);
        conversationRepository.save(conversation);

        log.info("Closed conversation {} - Reason: {}", conversation.getId(), endReason);
    }

    /**
     * Summarize conversation
     * In a real implementation, this would use AI to generate a summary
     */
    private void summarizeConversation(Conversation conversation) {
        // Calculate conversation duration
        LocalDateTime createdAt = conversation.getCreatedAt();
        LocalDateTime closedAt = LocalDateTime.now();
        long durationMinutes = ChronoUnit.MINUTES.between(createdAt, closedAt);

        // In a real implementation, this would:
        // 1. Fetch all messages in the conversation
        // 2. Use AI/LLM to generate a summary
        // 3. Store the summary in the conversation or a separate table

        String summary = String.format(
            "Conversation with %s (%s) lasted %d minutes. Channel: %s. Status: %s.",
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId(),
            conversation.getExternalUserId(),
            durationMinutes,
            conversation.getChannel(),
            conversation.getStatus()
        );

        log.info("Conversation summary for {}: {}", conversation.getId(), summary);

        conversation.setSummary(summary);
        conversationRepository.save(conversation);
    }

    /**
     * Offer follow-up actions to user
     */
    private void offerFollowUpActions(Conversation conversation) {
        String followUpMessage = "Thank you for your conversation! " +
                               "If you need further assistance, feel free to reach out anytime. " +
                               "We're here to help 24/7.";

        log.info("Sending follow-up message to user in conversation {}: {}",
            conversation.getId(), followUpMessage);

        try {
            channelMessengerService.sendMessage(conversation.getConnectionId(), conversation.getExternalUserId(), followUpMessage);
            log.info("Follow-up message sent for conversation {}", conversation.getId());
        } catch (Exception e) {
            log.error("Error in offerFollowUpActions for conversation {}: {}", conversation.getId(), e.getMessage());
        }
    }

    /**
     * Update customer data based on conversation
     * Note: CRM integration removed - placeholder for future integration
     */
    private void updateCustomerData(Conversation conversation) {
        log.info("Customer data update placeholder for conversation {}", conversation.getId());
        // CRM integration removed - this is a placeholder for future integration
        // if needed, implement using event-driven architecture or service contracts
    }


    /**
     * Notify agents that conversation has ended
     */
    private void notifyAgentsConversationEnded(Conversation conversation, String endReason) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "conversation_ended");
        notification.put("title", "Conversation Ended");
        notification.put("message", String.format(
            "Conversation %d has ended. Reason: %s. Customer: %s",
            conversation.getId(),
            endReason,
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId()
        ));
        notification.put("conversationId", conversation.getId());
        notification.put("endReason", endReason);
        notification.put("priority", "low");
        notification.put("timestamp", LocalDateTime.now());

        notificationWebSocketHandler.broadcastToTenant(conversation.getTenantId(), notification);

        log.info("Notified agents about conversation end for {}", conversation.getId());
    }

    /**
     * Reopen a closed conversation
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public void reopenConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found for reopen: " + conversationId));

        log.info("Reopening conversation {}", conversationId);

        conversation.setStatus("open");
        conversationRepository.save(conversation);

        // Notify agents
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "conversation_reopened");
        notification.put("title", "Conversation Reopened");
        notification.put("message", String.format(
            "Conversation %d has been reopened. Customer: %s",
            conversation.getId(),
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId()
        ));
        notification.put("conversationId", conversation.getId());
        notification.put("priority", "medium");
        notification.put("timestamp", LocalDateTime.now());

        notificationWebSocketHandler.broadcastToTenant(conversation.getTenantId(), notification);
    }

    /**
     * Generate conversation report
     */
    public Map<String, Object> generateConversationReport(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));

        Map<String, Object> report = new HashMap<>();
        report.put("conversationId", conversation.getId());
        report.put("externalUserId", conversation.getExternalUserId());
        report.put("userName", conversation.getUserName());
        report.put("channel", conversation.getChannel());
        report.put("status", conversation.getStatus());
        report.put("createdAt", conversation.getCreatedAt());
        report.put("updatedAt", conversation.getUpdatedAt());
        report.put("isTakenOverByAgent", conversation.getIsTakenOverByAgent());
        report.put("agentAssignedId", conversation.getAgentAssignedId());
        report.put("customerTier", conversation.getCustomerTier());
        report.put("language", conversation.getLanguage());

        // Calculate duration if closed
        if ("closed".equals(conversation.getStatus())) {
            LocalDateTime createdAt = conversation.getCreatedAt();
            LocalDateTime closedAt = conversation.getUpdatedAt();
            long durationMinutes = ChronoUnit.MINUTES.between(createdAt, closedAt);
            report.put("durationMinutes", durationMinutes);
        }

        // SLA metrics
        if (conversation.getFirstAgentResponseTime() != null) {
            long responseTime = ChronoUnit.SECONDS.between(
                conversation.getCreatedAt(),
                conversation.getFirstAgentResponseTime()
            );
            report.put("firstAgentResponseTimeSeconds", responseTime);
        }

        if (conversation.getFirstBotResponseTime() != null) {
            long responseTime = ChronoUnit.SECONDS.between(
                conversation.getCreatedAt(),
                conversation.getFirstBotResponseTime()
            );
            report.put("firstBotResponseTimeSeconds", responseTime);
        }

        report.put("slaBreachCount", conversation.getSlaBreachCount());

        return report;
    }
}
