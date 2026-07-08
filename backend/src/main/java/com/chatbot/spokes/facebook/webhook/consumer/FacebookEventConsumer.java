package com.chatbot.spokes.facebook.webhook.consumer;

import com.chatbot.config.KafkaConfig;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.Channel;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.messenger.service.FacebookMessengerService;
import com.chatbot.spokes.facebook.webhook.dto.WebhookRequest;
import com.chatbot.spokes.facebook.webhook.dto.FacebookKafkaEvent;
import com.chatbot.spokes.facebook.webhook.model.FacebookMessageType;
import com.chatbot.shared.penny.service.PennyBotManager;
import com.chatbot.core.message.store.service.ConversationService;
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.core.message.store.service.AIEscalationService;
import com.chatbot.core.message.store.service.ErrorWorkflow;
import com.chatbot.core.message.store.service.ConversationEndWorkflow;
import com.chatbot.core.message.decision.service.TakeoverService;
import com.chatbot.core.message.decision.model.TakeoverMessage;
import com.chatbot.spokes.odoo.service.CustomerDataService;
import com.chatbot.core.tenant.infra.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Kafka consumer that processes Facebook webhook events asynchronously with full business logic.
 * Guaranteed ordered processing per conversation partition (runs synchronously on consumer thread).
 */
@Service
@Slf4j
@lombok.RequiredArgsConstructor
public class FacebookEventConsumer {

    private final FacebookConnectionRepository connectionRepository;
    private final PennyBotManager pennyBotManager;
    private final FacebookMessengerService facebookMessengerService;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final TakeoverService takeoverService;
    private final CustomerDataService customerDataService;
    private final AIEscalationService aiEscalationService;
    private final ErrorWorkflow errorWorkflow;
    private final ConversationEndWorkflow conversationEndWorkflow;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    // Caffeine Cache with 15-minute TTL for in-memory deduplication fallback
    private final Cache<String, Boolean> dedupCache = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();



    private boolean tryDedup(String mid) {
        if (mid == null) return false;
        try {
            String key = "facebook:dedup:mid:" + mid;
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "true", java.time.Duration.ofMinutes(15));
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.error("❌ Failed to check deduplication in Redis: {}, falling back to Caffeine cache", e.getMessage());
            return dedupCache.getIfPresent(mid) == null && dedupCache.asMap().putIfAbsent(mid, true) == null;
        }
    }

    @KafkaListener(topics = KafkaConfig.FACEBOOK_EVENT_TOPIC, groupId = "facebook-consumer-group")
    public void consume(String messageJson) {
        FacebookKafkaEvent event;
        try {
            event = objectMapper.readValue(messageJson, FacebookKafkaEvent.class);
        } catch (Exception e) {
            log.error("❌ [Kafka Consumer] Failed to deserialize JSON: {}", e.getMessage(), e);
            return;
        }

        if (event == null || event.getMessaging() == null) {
            log.warn("⚠️ [Kafka Consumer] Received empty or invalid event.");
            return;
        }

        // Set tenant context for thread-safety and partition routing
        TenantContext.setTenantId(event.getTenantId());
        log.info("📥 [Kafka Consumer] Processing event. Tenant: {}, User: {}", event.getTenantId(), event.getSenderId());

        try {
            // Get connection with tenantId and pageId
            Optional<FacebookConnection> connectionOpt = connectionRepository.findByTenantIdAndPageId(
                    event.getTenantId(), event.getPageId()
            );

            if (connectionOpt.isEmpty() || !connectionOpt.get().isEnabled()) {
                log.warn("⚠️ Connection disabled or not found for Tenant: {}, Page: {}", event.getTenantId(), event.getPageId());
                return;
            }

            FacebookConnection connection = connectionOpt.get();
            WebhookRequest.Messaging messaging = event.getMessaging();
            FacebookMessageType type = classifyMessage(messaging);

            switch (type) {
                case TEXT:
                    handleTextMessage(connection, event.getSenderId(), messaging.getMessage());
                    break;
                case IMAGE:
                case VIDEO:
                case AUDIO:
                case FILE:
                case ATTACHMENT:
                    handleAttachmentMessage(connection, event.getSenderId(), messaging);
                    break;
                case QUICK_REPLY:
                    handleQuickReply(connection, event.getSenderId(), messaging);
                    break;
                case POSTBACK:
                    handlePostback(connection, event.getSenderId(), messaging);
                    break;
                case REACTION:
                    handleReaction(connection, event.getSenderId(), messaging);
                    break;
                case READ:
                    handleRead(messaging);
                    break;
                case DELIVERY:
                    handleDelivery(messaging);
                    break;
                default:
                    log.info("⚠️ Unknown message type, skipping.");
            }
        } catch (Exception e) {
            log.error("❌ [Kafka Consumer] Processing exception: {}", e.getMessage(), e);

            // Do NOT remove dedup key - let it expire naturally after 15 minutes
            // This preserves dedup protection when message goes to DLQ after retries
            // Kafka's retry mechanism will handle retries without needing to remove the key

            throw new RuntimeException("Error processing Kafka event", e);
        } finally {
            // Guarantee TenantContext clean-up to prevent context leaking
            TenantContext.clear();
        }
    }

    // ========== MESSAGE HANDLERS ==========

    private void handleTextMessage(FacebookConnection connection, String senderId, WebhookRequest.Message message) {
        String mid = message.getMid();
        String text = message.getText();
        if (text == null || text.isEmpty() || mid == null) return;

        if (!tryDedup(mid)) {
            log.info("⚠️ Skipping duplicate message mid=" + mid);
            return;
        }

        log.info("✉️ Processing TEXT: " + text);

        UUID connectionId = connection.getId();
        Channel channel = Channel.FACEBOOK;

        Optional<Conversation> existingConvOpt = conversationService.findByConnectionIdAndExternalUserId(connectionId, senderId);
        Conversation conversation = existingConvOpt.orElseGet(() -> conversationService.findOrCreate(connectionId, senderId, channel));

        Message savedMessage = null;
        try {
            savedMessage = messageService.saveMessage(
                    conversation.getId(),
                    "user",
                    text,
                    FacebookMessageType.TEXT.name(),
                    Map.of("mid", mid)
            );
            log.info("✅ Saved user text message to DB. Conversation ID: " + conversation.getId() + ", Message ID: " + savedMessage.getId());
        } catch (Exception e) {
            log.error("❌ Failed to save user text message to DB: " + e.getMessage());
            // Trigger error workflow for message save failure
            errorWorkflow.handleError(conversation.getId(), "MESSAGE_SAVE_FAILED",
                    "Failed to save user message: " + e.getMessage(), "medium");
        }

        try {
            customerDataService.processAndAccumulate(connection.getPageId(), senderId, text);
        } catch (Exception e) {
            log.error("❌ Customer data sync failed: {}", e.getMessage());
        }

        routeToPennyBot(connection, senderId, text, "text", conversation, savedMessage);
    }

    private void handleAttachmentMessage(FacebookConnection connection, String senderId, WebhookRequest.Messaging messaging) {
        String mid = messaging.getMessage().getMid();
        if (mid == null || !tryDedup(mid)) {
            log.info("⚠️ Skipping duplicate attachment mid=" + mid);
            return;
        }

        UUID connectionId = connection.getId();
        Channel channel = Channel.FACEBOOK;
        Conversation conversation = conversationService.findOrCreate(connectionId, senderId, channel);

        messaging.getMessage().getAttachments().forEach(attachment -> {
            String type = attachment.getType();
            String url = attachment.getPayload() != null ? attachment.getPayload().getUrl() : null;

            if (url != null) {
                log.info("🖼 ATTACHMENT: type=" + type + ", url=" + url);

                Message savedMessage = null;
                try {
                    savedMessage = messageService.saveMessage(
                            conversation.getId(),
                            "user",
                            "[" + type.toUpperCase() + "]",
                            FacebookMessageType.ATTACHMENT.name(),
                            Map.of("mid", mid, "url", url, "type", type)
                    );
                    log.info("✅ Saved attachment message to DB. Message ID: " + savedMessage.getId());
                } catch (Exception e) {
                    log.error("❌ Failed to save attachment to DB: " + e.getMessage());
                }

                String attachmentText = "[" + type.toUpperCase() + "]";
                routeToPennyBot(connection, senderId, attachmentText + " (" + url + ")", type, conversation, savedMessage);
            }
        });
    }

    private void handleQuickReply(FacebookConnection connection, String senderId, WebhookRequest.Messaging messaging) {
        String payload = messaging.getMessage().getQuickReply().getPayload();
        String text = messaging.getMessage().getText();
        String messageContent = text != null && !text.isEmpty() ? text : payload;
        String mid = messaging.getMessage().getMid();

        UUID connectionId = connection.getId();
        Channel channel = Channel.FACEBOOK;
        Conversation conversation = conversationService.findOrCreate(connectionId, senderId, channel);

        Message savedMessage = null;
        try {
            savedMessage = messageService.saveMessage(
                    conversation.getId(),
                    "user",
                    messageContent,
                    FacebookMessageType.QUICK_REPLY.name(),
                    Map.of("payload", payload, "mid", mid)
            );
            log.info("✅ Saved QuickReply to DB. Message ID: " + savedMessage.getId());
        } catch (Exception e) {
            log.error("❌ Failed to save QuickReply to DB: " + e.getMessage());
        }

        try {
            customerDataService.processAndAccumulate(connection.getPageId(), senderId, messageContent);
        } catch (Exception e) {
            log.error("❌ Customer data sync failed: {}", e.getMessage());
        }

        routeToPennyBot(connection, senderId, "[QuickReply] " + payload, "quick_reply", conversation, savedMessage);
    }

    private void handlePostback(FacebookConnection connection, String senderId, WebhookRequest.Messaging messaging) {
        String payload = messaging.getPostback().getPayload();
        String title = messaging.getPostback().getTitle();
        String text = title != null ? title : "[Postback]";

        UUID connectionId = connection.getId();
        Channel channel = Channel.FACEBOOK;
        Conversation conversation = conversationService.findOrCreate(connectionId, senderId, channel);

        Message savedMessage = null;
        try {
            savedMessage = messageService.saveMessage(
                    conversation.getId(),
                    "user",
                    text,
                    FacebookMessageType.POSTBACK.name(),
                    Map.of("payload", payload)
            );
            log.info("✅ Saved Postback to DB. Message ID: " + savedMessage.getId());
        } catch (Exception e) {
            log.error("❌ Failed to save Postback to DB: " + e.getMessage());
        }

        try {
            customerDataService.processAndAccumulate(connection.getPageId(), senderId, text);
        } catch (Exception e) {
            log.error("❌ Customer data sync failed: {}", e.getMessage());
        }

        routeToPennyBot(connection, senderId, "[Postback] " + payload, "postback", conversation, savedMessage);
    }

    private void handleReaction(FacebookConnection connection, String senderId, WebhookRequest.Messaging messaging) {
        if (messaging.getReaction() == null || messaging.getReaction().getEmoji() == null) return;
        String action = messaging.getReaction().getAction();
        String emoji = messaging.getReaction().getEmoji();
        String mid = messaging.getReaction().getMid();

        if (mid == null || !tryDedup(mid)) return;
        log.info("❤️ REACTION: action=" + action + ", emoji=" + emoji);
    }

    private void handleRead(WebhookRequest.Messaging messaging) {
        log.info("👀 READ: watermark=" + messaging.getRead().getWatermark());
    }

    private void handleDelivery(WebhookRequest.Messaging messaging) {
        log.info("📬 DELIVERY: mids=" + messaging.getDelivery().getMids());
    }

    /**
     * Route event to PennyBot if not taken over by human Agent
     */
    private void routeToPennyBot(FacebookConnection connection, String senderId, String messageText, String messageType, Conversation conversation, Message userMessage) {
        log.info("🤖 [Penny] Starting message processing...");

        try {
            // Save to Redis for Takeover history using real DB message ID
                String messageId = (userMessage != null) ? String.valueOf(userMessage.getId()) : "user_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
                TakeoverMessage takeoverMessage = new TakeoverMessage(
                        messageId,
                        String.valueOf(conversation.getId()),
                        "user",
                        messageText,
                        System.currentTimeMillis()
                );
                takeoverService.saveMessage(takeoverMessage);

                // Publish via WebSocket to UI
                try {
                    takeoverService.sendToConversation(takeoverMessage);
                } catch (Exception e) {
                    log.error("❌ WebSocket push error for user message: {}", e.getMessage());
                }

                // Check if conversation is taken over by Agent (reuse conversation object)
                boolean isTakenOver = conversation.getIsTakenOverByAgent();

                if (isTakenOver) {
                    log.info("🛑 Conversation {} is taken over by Agent. Skipping Bot reply.", conversation.getId());
                    return;
                }

                // Route to Penny Bot
                if (connection.getBotId() == null || connection.getBotId().trim().isEmpty()) {
                    log.warn("⚠️ Connection has no botId. Skipping bot processing.");
                    return;
                }

                UUID botId = UUID.fromString(connection.getBotId());
                String botReply = null;
                try {
                    botReply = pennyBotManager.processMessage(botId, messageText, connection.getOwnerId(), false);
                } catch (IllegalArgumentException e) {
                    log.error("❌ Bot not found for connection {}: {}. Sending fallback response.", connection.getId(), e.getMessage());
                    botReply = "Sorry, the bot configuration is missing. Please contact the administrator.";
                    // Trigger error workflow - bot configuration missing
                    errorWorkflow.handleError(conversation.getId(), "BOT_NOT_CONFIGURED",
                            "Bot not found for connection " + connection.getId() + ": " + e.getMessage(), "medium");
                } catch (Exception e) {
                    log.error("❌ Error processing message with bot {}: {}", botId, e.getMessage(), e);
                    botReply = "Sorry, I encountered an error while processing your message. Please try again.";
                    // Trigger error workflow - bot processing failed
                    errorWorkflow.handleError(conversation.getId(), "BOT_PROCESSING_FAILED",
                            "Bot processing error: " + e.getMessage(), "high");
                }

                if (botReply != null && !botReply.trim().isEmpty()) {
                    log.info("✅ [Penny] Bot handled message. Sending response...");

                    // Save Bot Message to DB
                    Message botMessageSaved = null;
                    try {
                        botMessageSaved = messageService.saveMessage(
                                conversation.getId(),
                                "bot",
                                botReply,
                                FacebookMessageType.TEXT.name(),
                                null
                        );
                        log.info("✅ Saved bot message to DB. Message ID: " + botMessageSaved.getId());
                    } catch (Exception e) {
                        log.error("❌ Failed to save bot message to DB: " + e.getMessage());
                    }

                    // Send to Facebook
                    try {
                        facebookMessengerService.sendTextMessage(connection.getPageAccessToken(), senderId, botReply);
                        log.info("✅ Sent bot reply to Facebook. Conversation ID: " + conversation.getId());
                    } catch (Exception e) {
                        log.error("❌ Failed to send bot reply to Facebook: " + e.getMessage());
                    }

                    // Save bot message to Redis for Takeover history
                    if (botMessageSaved != null) {
                        TakeoverMessage botTakeoverMessage = new TakeoverMessage(
                                String.valueOf(botMessageSaved.getId()),
                                String.valueOf(conversation.getId()),
                                "bot",
                                botReply,
                                System.currentTimeMillis()
                        );
                        takeoverService.saveMessage(botTakeoverMessage);

                        // Publish bot message via WebSocket
                        try {
                            takeoverService.sendToConversation(botTakeoverMessage);
                        } catch (Exception e) {
                            log.error("❌ WebSocket push error for bot message: {}", e.getMessage());
                        }
                    }
                }

                // AI-based Escalation Analysis
                // Trigger AI analysis after bot reply to determine if escalation is needed
                try {
                    aiEscalationService.analyzeAndEscalateIfNeeded(conversation.getId());
                } catch (Exception e) {
                    log.error("❌ AI escalation analysis failed: {}", e.getMessage());
                }
        } catch (Exception e) {
            log.error("❌ Penny processing error: {}", e.getMessage(), e);
            throw new RuntimeException("Penny processing failed", e);
        }
    }

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
}
