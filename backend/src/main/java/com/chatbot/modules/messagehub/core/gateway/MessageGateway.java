package com.chatbot.modules.messagehub.core.gateway;

import com.chatbot.modules.messagehub.core.dto.MessageRequest;
import com.chatbot.modules.messagehub.core.dto.MessageResponse;
import com.chatbot.modules.messagehub.core.service.ContextStore;
import com.chatbot.modules.messagehub.core.service.DecisionService;
import com.chatbot.modules.messagehub.core.model.ConversationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Message Gateway for Message Hub MVP
 * Single entry point for all messages from adapters/apps
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageGateway {

    private final ContextStore contextStore;
    private final DecisionService decisionService;

    /**
     * Process incoming message from adapters
     * This is the ONLY entry point for messages
     */
    public MessageResponse processMessage(MessageRequest request) {
        log.info("Processing message for conversation: {}, user: {}, tenant: {}", 
                request.getConversationId(), request.getUserId(), request.getTenantId());

        // Validate request
        if (!request.isValid()) {
            log.warn("Invalid message request: {}", request);
            return MessageResponse.blocked("Invalid message request");
        }

        try {
            // Get or create conversation context
            ConversationContext context = contextStore.getOrCreateContext(
                    request.getConversationId(),
                    request.getUserId(),
                    request.getTenantId()
            );

            // Make decision
            DecisionService.DecisionResult decision = decisionService.makeDecision(context, request.getIntent(), request);

            // Update context if needed
            if (decision.shouldUpdateContext()) {
                contextStore.updateContext(context);
            }

            // Return response based on decision
            if ("BOT_PROCESS".equals(decision.getDecision())) {
                // Nếu Penny bot đã xử lý, lấy response từ DecisionResult
                MessageResponse pennyResponse = decision.getPennyResponse();
                if (pennyResponse != null) {
                    return pennyResponse;
                }
            }
            
            return switch (decision.getDecision()) {
                case "BOT_PROCESS" -> MessageResponse.botProcess("Message processed by bot");
                case "HUMAN_REQUIRED" -> MessageResponse.humanRequired(decision.getReason());
                case "BLOCKED" -> MessageResponse.blocked(decision.getReason());
                default -> MessageResponse.blocked("Unknown decision");
            };

        } catch (Exception e) {
            log.error("Error processing message for conversation: {}", request.getConversationId(), e);
            return MessageResponse.blocked("Internal error processing message");
        }
    }

    /**
     * Get current conversation context
     */
    public ConversationContext getContext(String conversationId) {
        return contextStore.getContext(conversationId).orElse(null);
    }
}
