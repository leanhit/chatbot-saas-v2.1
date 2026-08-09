package com.chatbot.core.message.store.service;

import com.chatbot.core.message.decision.exception.ConversationNotFoundException;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.notification.websocket.NotificationWebSocketHandler;
import com.chatbot.shared.messenger.ChannelMessengerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling error scenarios in conversation processing
 * Implements Phase 2.3: Special Workflows - ErrorWorkflow
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ErrorWorkflow {

    private final ConversationRepository conversationRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final ChannelMessengerService channelMessengerService;

    /**
     * Handle error in conversation processing
     * @param conversationId The conversation ID where error occurred
     * @param errorType Type of error (e.g., "BOT_PROCESSING_FAILED", "MESSAGE_SEND_FAILED", "API_ERROR")
     * @param errorMessage Error message
     * @param severity Error severity (low, medium, high, critical)
     */
    public void handleError(Long conversationId, String errorType, String errorMessage, String severity) {
        log.error("Error in conversation {}: {} - {}", conversationId, errorType, errorMessage);

        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found for error handling: " + conversationId));

        // Log error details
        logErrorToConversation(conversation, errorType, errorMessage, severity);

        // Notify admin based on severity
        if ("high".equals(severity) || "critical".equals(severity)) {
            notifyAdmin(conversation, errorType, errorMessage, severity);
        }

        // Send fallback message to user if needed
        if (shouldSendFallbackMessage(errorType, severity)) {
            sendFallbackMessage(conversation, errorType);
        }

        // Create escalation ticket for critical errors
        if ("critical".equals(severity)) {
            createEscalationTicket(conversation, errorType, errorMessage);
        }
    }

    /**
     * Log error to conversation (could be stored in customAttributes or a separate error log table)
     */
    private void logErrorToConversation(Conversation conversation, String errorType, String errorMessage, String severity) {
        try {
            // Add error entry
            Map<String, Object> errorEntry = new HashMap<>();
            errorEntry.put("errorType", errorType);
            errorEntry.put("errorMessage", errorMessage);
            errorEntry.put("severity", severity);
            errorEntry.put("timestamp", LocalDateTime.now());

            // In a real implementation, this would be stored properly in customAttributes or error log table
            log.info("Logged error to conversation {}: {}", conversation.getId(), errorEntry);

        } catch (Exception e) {
            log.error("Failed to log error to conversation", e);
        }
    }

    /**
     * Notify admin about error
     */
    private void notifyAdmin(Conversation conversation, String errorType, String errorMessage, String severity) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "error_alert");
        notification.put("title", "Conversation Error Alert");
        notification.put("message", String.format(
            "Error in conversation %d: %s - %s. Severity: %s. Customer: %s",
            conversation.getId(),
            errorType,
            errorMessage,
            severity,
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId()
        ));
        notification.put("conversationId", conversation.getId());
        notification.put("errorType", errorType);
        notification.put("severity", severity);
        notification.put("priority", "high".equals(severity) ? "high" : "urgent");
        notification.put("timestamp", LocalDateTime.now());

        notificationWebSocketHandler.broadcastToTenant(conversation.getTenantId(), notification);

        log.info("Notified admin about error in conversation {}", conversation.getId());
    }

    /**
     * Determine if fallback message should be sent to user
     */
    private boolean shouldSendFallbackMessage(String errorType, String severity) {
        // Send fallback for processing errors
        return "BOT_PROCESSING_FAILED".equals(errorType) ||
               "API_ERROR".equals(errorType) ||
               "critical".equals(severity);
    }

    /**
     * Send fallback message to user
     */
    private void sendFallbackMessage(Conversation conversation, String errorType) {
        String fallbackMessage = getFallbackMessage(errorType);

        log.info("Sending fallback message to user in conversation {}: {}",
            conversation.getId(), fallbackMessage);

        try {
            channelMessengerService.sendMessage(conversation.getConnectionId(), conversation.getExternalUserId(), fallbackMessage);
            log.info("Fallback message sent for conversation {}", conversation.getId());
        } catch (Exception e) {
            log.error("Error in sendFallbackMessage for conversation {}: {}", conversation.getId(), e.getMessage());
        }
    }

    /**
     * Get appropriate fallback message based on error type
     */
    private String getFallbackMessage(String errorType) {
        switch (errorType) {
            case "BOT_PROCESSING_FAILED":
                return "We're experiencing some technical difficulties. Our team has been notified and will assist you shortly.";
            case "MESSAGE_SEND_FAILED":
                return "We couldn't deliver your message. Please try again in a moment.";
            case "API_ERROR":
                return "Our system is temporarily unavailable. Please try again later.";
            default:
                return "Something went wrong. Our team has been notified and is working on it.";
        }
    }

    /**
     * Create escalation ticket for critical errors
     */
    private void createEscalationTicket(Conversation conversation, String errorType, String errorMessage) {
        log.warn("Creating escalation ticket for critical error in conversation {}", conversation.getId());

        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "escalation_required");
        notification.put("title", "Critical Error - Immediate Attention Required");
        notification.put("message", String.format(
            "Critical error in conversation %d requires immediate escalation. Error: %s - %s",
            conversation.getId(),
            errorType,
            errorMessage
        ));
        notification.put("conversationId", conversation.getId());
        notification.put("errorType", errorType);
        notification.put("priority", "urgent");
        notification.put("timestamp", LocalDateTime.now());

        notificationWebSocketHandler.broadcastToTenant(conversation.getTenantId(), notification);

        // Fallback for Ticket System: Log the escalation as a critical error entry in the conversation
        log.info("Ticket System Fallback: Simulating ticket creation for conversation {}", conversation.getId());
        logErrorToConversation(conversation, "ESCALATION_TICKET_CREATED", "Ticket created for error: " + errorType, "critical");
    }

    /**
     * Recover from error - attempt to resume normal processing
     */
    public void recoverFromError(Long conversationId) {
        log.info("Attempting to recover from error in conversation {}", conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));

        // Check if conversation can be resumed
        if ("open".equals(conversation.getStatus())) {
            log.info("Conversation {} is open, can resume normal processing", conversationId);
            // Fallback: Notify admins that conversation is ready to be resumed
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "recovery_ready");
            notification.put("message", "Conversation " + conversationId + " is ready for recovery.");
            notification.put("conversationId", conversationId);
            notification.put("timestamp", LocalDateTime.now());
            notificationWebSocketHandler.broadcastToTenant(conversation.getTenantId(), notification);
        }
    }
}
