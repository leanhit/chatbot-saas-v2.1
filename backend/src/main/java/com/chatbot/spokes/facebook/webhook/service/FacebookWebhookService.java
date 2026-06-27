package com.chatbot.spokes.facebook.webhook.service;

import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.webhook.dto.WebhookRequest;
import com.chatbot.spokes.facebook.webhook.dto.FacebookKafkaEvent;
import com.chatbot.spokes.facebook.webhook.model.FacebookMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Webhook service that receives incoming Facebook webhook payloads,
 * resolves tenant context, filters echos, and publishes individual events
 * to Kafka for asynchronous processing.
 */
@Service
@Slf4j
public class FacebookWebhookService {

    private final FacebookKafkaProducer kafkaProducer;
    private final FacebookConnectionRepository connectionRepository;

    @Value("${facebook.webhook.verify-token:your_facebook_verify_token}")
    private String verifyToken;

    public FacebookWebhookService(FacebookKafkaProducer kafkaProducer, 
                                  FacebookConnectionRepository connectionRepository) {
        this.kafkaProducer = kafkaProducer;
        this.connectionRepository = connectionRepository;
    }

    /** Verify webhook request from Facebook */
    public boolean verifyWebhook(String mode, String challenge, String verifyToken) {
        log.info("Verifying webhook - mode: {}, challenge: {}, token: {}", mode, challenge, verifyToken);
        return "subscribe".equals(mode) && this.verifyToken.equals(verifyToken);
    }

    /** Forward incoming webhook payload to Kafka for async processing */
    public void handleWebhookEvent(WebhookRequest request) {
        if (!"page".equals(request.getObject())) return;

        for (WebhookRequest.Entry entry : request.getEntry()) {
            if (entry.getMessaging() == null) continue;

            String pageId = entry.getId();
            Long tenantId = findTenantIdByPageId(pageId);
            if (tenantId == null) {
                log.warn("⚠️ No tenant ID found for page: {}, skipping events.", pageId);
                continue;
            }

            for (WebhookRequest.Messaging messaging : entry.getMessaging()) {
                FacebookMessageType type = classifyMessage(messaging);

                // Skip ECHO messages at producer to avoid queue flood
                if (type == FacebookMessageType.ECHO) {
                    log.info("🔄 Skipping ECHO message: " + (messaging.getMessage() != null ? messaging.getMessage().getText() : ""));
                    continue;
                }

                // Determine senderId (user PSID) and actual pageId
                String actualPageId = (messaging.getMessage() != null && Boolean.TRUE.equals(messaging.getMessage().getIsEcho()))
                        ? messaging.getSender().getId()
                        : messaging.getRecipient().getId();
                String senderId = (messaging.getMessage() != null && Boolean.TRUE.equals(messaging.getMessage().getIsEcho()))
                        ? messaging.getRecipient().getId()
                        : messaging.getSender().getId();

                FacebookKafkaEvent event = FacebookKafkaEvent.builder()
                        .tenantId(tenantId)
                        .pageId(actualPageId)
                        .senderId(senderId)
                        .messaging(messaging)
                        .build();

                // Publish to Kafka using user senderId as partition key
                kafkaProducer.send(senderId, event);
                log.info("✅ [Kafka Producer] Webhook event published. Page: {}, User (Key): {}", actualPageId, senderId);
            }
        }
    }

    /**
     * Classify message type
     */
    private FacebookMessageType classifyMessage(WebhookRequest.Messaging messaging) {
        if (messaging.getMessage() != null) {
            if (Boolean.TRUE.equals(messaging.getMessage().getIsEcho())) return FacebookMessageType.ECHO;
            if (messaging.getMessage().getQuickReply() != null) return FacebookMessageType.QUICK_REPLY;
            if (messaging.getMessage().getText() != null) return FacebookMessageType.TEXT;
            if (messaging.getMessage().getAttachments() != null && !messaging.getMessage().getAttachments().isEmpty()) {
                String type = messaging.getMessage().getAttachments().get(0).getType();
                switch (type) {
                    case "image": return FacebookMessageType.IMAGE;
                    case "video": return FacebookMessageType.VIDEO;
                    case "audio": return FacebookMessageType.AUDIO;
                    case "file": return FacebookMessageType.FILE;
                    default: return FacebookMessageType.ATTACHMENT;
                }
            }
        } else if (messaging.getPostback() != null) return FacebookMessageType.POSTBACK;
        else if (messaging.getReaction() != null) return FacebookMessageType.REACTION;
        else if (messaging.getRead() != null) return FacebookMessageType.READ;
        else if (messaging.getDelivery() != null) return FacebookMessageType.DELIVERY;

        return FacebookMessageType.UNKNOWN;
    }

    /**
     * Find tenantId by querying FacebookConnection
     */
    private Long findTenantIdByPageId(String pageId) {
        // 1. Try to find active and enabled connection(s)
        List<FacebookConnection> activeConnections = connectionRepository.findByPageIdForWebhook(pageId);
        if (!activeConnections.isEmpty()) {
            if (activeConnections.size() > 1) {
                log.warn("⚠️ Multiple active connections found for pageId: {} ({} connections). Using first one.", 
                    pageId, activeConnections.size());
            }
            return activeConnections.get(0).getTenantId();
        }

        // 2. Fallback to any connection
        List<FacebookConnection> allConnections = connectionRepository.findAllByPageId(pageId);
        if (!allConnections.isEmpty()) {
            log.warn("⚠️ No active connections found for pageId: {}. Using inactive connection as fallback.", pageId);
            return allConnections.get(0).getTenantId();
        }

        return null;
    }
}
