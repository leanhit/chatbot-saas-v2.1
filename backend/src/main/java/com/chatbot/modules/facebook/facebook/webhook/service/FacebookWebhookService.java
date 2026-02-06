package com.chatbot.modules.facebook.facebook.webhook.service;

import com.chatbot.modules.facebook.facebook.webhook.dto.FacebookWebhookRequest;
import com.chatbot.modules.facebook.facebook.webhook.dto.FacebookMessaging;
import com.chatbot.modules.messagehub.core.dto.MessageRequest;
import com.chatbot.modules.messagehub.core.dto.MessageResponse;
import com.chatbot.modules.messagehub.core.gateway.MessageGateway;
import com.chatbot.modules.facebook.facebook.connection.model.FacebookConnection;
import com.chatbot.modules.facebook.facebook.connection.repository.FacebookConnectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

/**
 * Facebook Webhook Service for Phase 0 Integration
 * Routes Facebook messages through Message Hub
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FacebookWebhookService {

    private final MessageGateway messageGateway;
    private final FacebookConnectionRepository facebookConnectionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Process Facebook webhook message through Message Hub
     */
    public void processWebhookMessage(FacebookWebhookRequest webhookRequest) {
        log.info("Processing Facebook webhook message");

        if (!webhookRequest.isPageEvent() || !webhookRequest.hasMessages()) {
            log.warn("Invalid webhook request: not a page event or no messages");
            return;
        }

        FacebookMessaging messaging = webhookRequest.getFirstMessage();
        String pageId = messaging.getRecipient().getId();
        String senderId = messaging.getSender().getId();
        String messageText = messaging.getMessage() != null ? messaging.getMessage().getText() : "";

        // Find Facebook connection to get tenant info
        Optional<FacebookConnection> connectionOpt = facebookConnectionRepository.findByPageId(pageId);
        if (connectionOpt.isEmpty() || !connectionOpt.get().isEnabled()) {
            log.warn("No active Facebook connection found for pageId: {}", pageId);
            return;
        }
        
        FacebookConnection connection = connectionOpt.get();

        // Create MessageRequest for Message Hub
        MessageRequest messageRequest = MessageRequest.builder()
                .conversationId("fb_" + pageId + "_" + senderId)
                .userId(UUID.randomUUID()) // TODO: Map Facebook user to system user
                .tenantId(connection.getTenantId())
                .message(messageText)
                .senderType("user")
                .channel("facebook")
                .intent(extractIntent(messageText))
                .build();

        // Send to Message Hub
        MessageResponse response = messageGateway.processMessage(messageRequest);
        log.info("Message Hub decision: {} for conversation: {}", 
                response.getDecision(), messageRequest.getConversationId());

        // Route based on Message Hub decision
        routeBasedOnDecision(response, pageId, senderId, messageText);
    }

    /**
     * Route message based on Message Hub decision
     */
    private void routeBasedOnDecision(MessageResponse response, String pageId, String senderId, String messageText) {
        switch (response.getDecision()) {
            case "BOT_PROCESS":
                forwardToBotpress(pageId, senderId, messageText);
                break;
            case "HUMAN_REQUIRED":
                log.info("Human required for message - NOT forwarding to Botpress. Reason: {}", response.getReason());
                // TODO: Store in human inbox
                break;
            case "BLOCKED":
                log.warn("Message blocked: {}", response.getReason());
                break;
            default:
                log.warn("Unknown decision from Message Hub: {}", response.getDecision());
        }
    }

    /**
     * Forward message to Botpress
     */
    private void forwardToBotpress(String pageId, String senderId, String messageText) {
        try {
            // TODO: Implement actual Botpress API call
            log.info("Forwarding to Botpress - pageId: {}, senderId: {}, message: {}", 
                    pageId, senderId, messageText);
            
            // Botpress API endpoint (placeholder)
            String botpressUrl = "https://botpress.example.com/api/v1/bots/facebook/messages";
            
            // TODO: Implement actual Botpress payload format
            String payload = String.format("""
                {
                    "type": "message",
                    "channel": "facebook",
                    "recipient": {"id": "%s"},
                    "message": {"text": "%s"}
                }
                """, senderId, messageText);
            
            // restTemplate.postForObject(botpressUrl, payload, String.class);
            log.info("STUB: Would send to Botpress: {}", payload);
            
        } catch (Exception e) {
            log.error("Error forwarding to Botpress", e);
        }
    }

    /**
     * Extract simple intent from message text
     * Phase 0: Very basic intent detection
     */
    private String extractIntent(String messageText) {
        if (messageText == null || messageText.trim().isEmpty()) {
            return "unknown";
        }

        String lowerText = messageText.toLowerCase();
        
        // Basic intent detection for Phase 0
        if (lowerText.contains("giá") || lowerText.contains("price") || lowerText.contains("bao nhiêu")) {
            return "ask_price";
        }
        if (lowerText.contains("xin chào") || lowerText.contains("hello") || lowerText.contains("hi")) {
            return "greeting";
        }
        if (lowerText.contains("cảm ơn") || lowerText.contains("thank")) {
            return "thanks";
        }
        
        return "general";
    }
}
