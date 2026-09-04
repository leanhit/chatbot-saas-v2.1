package com.chatbot.core.message.store.service;

import com.chatbot.core.message.decision.exception.ConversationNotFoundException;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.notification.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for AI-based escalation decisions
 * Implements Phase 3.3: AI-based Escalation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIEscalationService {

    private final LLMClient llmClient;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final ConversationService conversationService;
    private final AgentAssignmentService agentAssignmentService;

    /**
     * Analyze conversation and determine if AI escalation is needed (Asynchronously)
     */
    @Async("taskExecutor")
    @Transactional
    public void analyzeAndEscalateIfNeeded(Long conversationId) {
        if (!llmClient.isEnabled()) {
            log.debug("LLM client is disabled, skipping AI escalation analysis");
            return;
        }

        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found for AI escalation: " + conversationId));

        // Skip if already taken over by agent
        if (conversation.getIsTakenOverByAgent()) {
            log.debug("Conversation {} already taken over by agent, skipping AI escalation", conversationId);
            return;
        }

        try {
            // Get recent conversation history
            String conversationText = getConversationText(conversationId, 10); // Last 10 messages

            // Analyze sentiment
            String sentiment = llmClient.analyzeSentiment(conversationText);
            log.info("AI sentiment analysis for conversation {}: {}", conversationId, sentiment);

            // Detect complexity
            String complexity = llmClient.detectComplexity(conversationText);
            log.info("AI complexity analysis for conversation {}: {}", conversationId, complexity);

            // Determine if escalation is needed
            boolean shouldEscalate = llmClient.shouldEscalate(
                conversationText,
                conversation.getCustomerTier(),
                sentiment
            );

            if (shouldEscalate) {
                log.info("AI recommends escalation for conversation {}", conversationId);
                handleAIEscalation(conversation, sentiment, complexity, conversationText);
            } else {
                log.debug("AI does not recommend escalation for conversation {}", conversationId);
            }

            // Store AI analysis results in conversation custom attributes
            storeAIAnalysisResults(conversation, sentiment, complexity, shouldEscalate);

        } catch (Exception e) {
            log.error("Error in AI escalation analysis for conversation {}", conversationId, e);
        }
    }

    /**
     * Get conversation text from recent messages
     */
    private String getConversationText(Long conversationId, int messageCount) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));

            List<com.chatbot.core.message.store.model.Message> messages = 
                messageRepository.findByConversationIdAndTenantId(
                    conversationId,
                    conversation.getTenantId()
                );
            
            if (messages == null || messages.isEmpty()) {
                return "";
            }

            // Get last N messages and reverse to chronological order
            StringBuilder text = new StringBuilder();
            int count = Math.min(messageCount, messages.size());
            for (int i = messages.size() - 1; i >= messages.size() - count; i--) {
                com.chatbot.core.message.store.model.Message msg = messages.get(i);
                text.append(msg.getSender())
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
            }

            return text.toString();
        } catch (Exception e) {
            log.error("Error getting conversation text", e);
            return "";
        }
    }

    /**
     * Handle AI-based escalation
     */
    private void handleAIEscalation(Conversation conversation, String sentiment, String complexity, String conversationText) {
        log.info("Handling AI escalation for conversation {}", conversation.getId());

        // Generate escalation summary
        String summary = llmClient.generateEscalationSummary(conversationText);
        
        // Try to auto-assign to best agent
        if (agentAssignmentService != null) {
            var bestAgent = agentAssignmentService.autoAssignConversation(conversation);
            if (bestAgent.isPresent()) {
                log.info("AI auto-assigned conversation {} to agent {}", conversation.getId(), bestAgent.get().getId());
                
                // Take over the conversation
                conversationService.takeoverConversation(conversation.getId(), bestAgent.get().getId());
                
                // Send notification about AI escalation
                sendAIEscalationNotification(conversation, bestAgent.get().getId(), sentiment, complexity, summary);
                return;
            }
        }

        // If no agent available, send escalation alert
        sendAIEscalationAlert(conversation, sentiment, complexity, summary);
    }

    /**
     * Store AI analysis results in conversation custom attributes
     */
    @SuppressWarnings("unchecked")
    private void storeAIAnalysisResults(Conversation conversation, String sentiment, String complexity, boolean shouldEscalate) {
        try {
            Map<String, Object> aiAnalysis = new HashMap<>();
            aiAnalysis.put("sentiment", sentiment);
            aiAnalysis.put("complexity", complexity);
            aiAnalysis.put("shouldEscalate", shouldEscalate);
            aiAnalysis.put("analyzedAt", LocalDateTime.now());

            String existingAttributes = conversation.getCustomAttributes();
            Map<String, Object> attributes;
            
            if (existingAttributes != null && !existingAttributes.isEmpty()) {
                attributes = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                    existingAttributes,
                    Map.class
                );
            } else {
                attributes = new HashMap<>();
            }

            attributes.put("aiAnalysis", aiAnalysis);
            conversation.setCustomAttributes(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(attributes));
            conversationRepository.save(conversation);

            log.info("Stored AI analysis results for conversation {}", conversation.getId());
        } catch (Exception e) {
            log.error("Error storing AI analysis results", e);
        }
    }

    /**
     * Send notification about AI escalation
     */
    private void sendAIEscalationNotification(Conversation conversation, Long agentId, String sentiment, String complexity, String summary) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ai_escalation");
        notification.put("title", "AI-Based Escalation");
        notification.put("message", String.format(
            "Conversation %d has been escalated to agent %d by AI. Sentiment: %s, Complexity: %s. Summary: %s",
            conversation.getId(),
            agentId,
            sentiment,
            complexity,
            summary
        ));
        notification.put("conversationId", conversation.getId());
        notification.put("agentId", agentId);
        notification.put("sentiment", sentiment);
        notification.put("complexity", complexity);
        notification.put("summary", summary);
        notification.put("priority", "high");
        notification.put("timestamp", LocalDateTime.now());

        notificationWebSocketHandler.broadcastToTenant(conversation.getTenantId(), notification);
    }

    /**
     * Send alert when AI recommends escalation but no agent available
     */
    private void sendAIEscalationAlert(Conversation conversation, String sentiment, String complexity, String summary) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ai_escalation_alert");
        notification.put("title", "AI Escalation Alert - No Agent Available");
        notification.put("message", String.format(
            "AI recommends escalation for conversation %d but no agent is available. Sentiment: %s, Complexity: %s. Summary: %s",
            conversation.getId(),
            sentiment,
            complexity,
            summary
        ));
        notification.put("conversationId", conversation.getId());
        notification.put("sentiment", sentiment);
        notification.put("complexity", complexity);
        notification.put("summary", summary);
        notification.put("priority", "urgent");
        notification.put("timestamp", LocalDateTime.now());

        notificationWebSocketHandler.broadcastToTenant(conversation.getTenantId(), notification);
    }

    /**
     * Get AI analysis results for a conversation
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAIAnalysis(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found: " + conversationId));
        if (conversation.getCustomAttributes() == null) {
            return null;
        }

        try {
            Map<String, Object> attributes = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                conversation.getCustomAttributes(),
                Map.class
            );
            return (Map<String, Object>) attributes.get("aiAnalysis");
        } catch (Exception e) {
            log.error("Error getting AI analysis", e);
            return null;
        }
    }

    /**
     * Manually trigger AI analysis for a conversation
     */
    @Transactional
    public void triggerAIAnalysis(Long conversationId) {
        log.info("Manually triggering AI analysis for conversation {}", conversationId);
        analyzeAndEscalateIfNeeded(conversationId);
    }

    /**
     * Get LLM client instance
     */
    public LLMClient getLLMClient() {
        return llmClient;
    }
}
