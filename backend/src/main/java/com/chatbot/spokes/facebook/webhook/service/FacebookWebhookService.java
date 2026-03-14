package com.chatbot.spokes.facebook.webhook.service;

import com.chatbot.spokes.facebook.webhook.dto.WebhookRequest;
import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.shared.penny.service.PennyBotManager;
import com.chatbot.spokes.facebook.webhook.model.FacebookMessageType;
import com.chatbot.spokes.facebook.messenger.service.FacebookMessengerService;
import com.chatbot.core.message.store.service.ConversationService;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.core.message.decision.service.TakeoverService;
import com.chatbot.core.message.decision.model.TakeoverMessage;
import com.chatbot.core.message.store.model.Channel;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.spokes.facebook.webhook.service.ChatbotServiceWrapper;
import com.chatbot.spokes.botpress.service.BotpressServiceFb;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Facebook Webhook Service
 * Processes webhook events from Facebook and routes to PennyBot
 * Following traloitudongV2 pattern but using PennyBot instead of Botpress
 */
@Service
@Slf4j
public class FacebookWebhookService {

    private final FacebookConnectionRepository connectionRepository;
    private final PennyBotManager pennyBotManager;
    private final FacebookMessengerService facebookMessengerService;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final TakeoverService takeoverService;
    private final ChatbotServiceWrapper chatbotServiceWrapper;
    private final BotpressServiceFb botpressServiceFb;

    @Value("${facebook.webhook.verify-token:your_facebook_verify_token}")
    private String verifyToken;

    private final Set<String> processedMessageIds = ConcurrentHashMap.newKeySet();
    
    // Cleanup processed message IDs periodically to prevent memory leak
    private static final long CLEANUP_INTERVAL_MS = 60 * 60 * 1000; // 1 hour
    private volatile long lastCleanupTime = System.currentTimeMillis();

    public FacebookWebhookService(FacebookConnectionRepository connectionRepository,
                                  PennyBotManager pennyBotManager,
                                  FacebookMessengerService facebookMessengerService,
                                  ConversationService conversationService,
                                  MessageService messageService,
                                  TakeoverService takeoverService,
                                  ChatbotServiceWrapper chatbotServiceWrapper,
                                  BotpressServiceFb botpressServiceFb) {
        this.connectionRepository = connectionRepository;
        this.pennyBotManager = pennyBotManager;
        this.facebookMessengerService = facebookMessengerService;
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.takeoverService = takeoverService;
        this.chatbotServiceWrapper = chatbotServiceWrapper;
        this.botpressServiceFb = botpressServiceFb;
    }

    /**
     * Verify webhook request from Facebook
     * Following traloitudongV2 pattern
     */
    public boolean verifyWebhook(String mode, String challenge, String verifyToken) {
        log.info("Verifying webhook - mode: {}, challenge: {}, token: {}", mode, challenge, verifyToken);
        
        return "subscribe".equals(mode) && this.verifyToken.equals(verifyToken);
    }

    /**
     * Classify message type
     * Following traloitudongV2 pattern
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
     * Find tenantId from pageId by querying FacebookConnection
     * Following traloitudongV2 pattern
     */
    private Long findTenantIdByPageId(String pageId) {
        // 1. Try to find active and enabled connection
        Optional<FacebookConnection> activeConnectionOpt = connectionRepository.findByPageIdForWebhook(pageId);
        if (activeConnectionOpt.isPresent()) {
            FacebookConnection connection = activeConnectionOpt.get();
            log.info("Found active connection for pageId: {} -> tenantId: {}", pageId, connection.getTenantId());
            return connection.getTenantId();
        }
        
        // 2. If no active connection, log all connections for debug
        List<FacebookConnection> allConnections = connectionRepository.findAllByPageId(pageId);
        if (allConnections.isEmpty()) {
            log.warn("No FacebookConnection found for pageId: {}", pageId);
            return null;
        }
        
        // 3. Log all connections for debug
        log.warn("PageId: {} has {} connections but no active connections:", pageId, allConnections.size());
        for (FacebookConnection conn : allConnections) {
            log.warn("  - TenantId: {}, Enabled: {}, Active: {}, OwnerId: {}", 
                    conn.getTenantId(), conn.isEnabled(), conn.isActive(), conn.getOwnerId());
        }
        
        // 4. Return tenant of first connection (fallback)
        Long fallbackTenantId = allConnections.get(0).getTenantId();
        log.warn("Using fallback tenantId: {} for pageId: {}", fallbackTenantId, pageId);
        return fallbackTenantId;
    }

    /**
     * Handle webhook event from Facebook
     * Following traloitudongV2 pattern
     */
    public void handleWebhookEvent(WebhookRequest request) {
        if (!"page".equals(request.getObject())) return;

        for (WebhookRequest.Entry entry : request.getEntry()) {
            if (entry.getMessaging() == null) continue;

            for (WebhookRequest.Messaging messaging : entry.getMessaging()) {
                // Determine pageId & senderId
                String pageId = (messaging.getMessage() != null && Boolean.TRUE.equals(messaging.getMessage().getIsEcho()))
                        ? messaging.getSender().getId()
                        : messaging.getRecipient().getId();
                String senderId = (messaging.getMessage() != null && Boolean.TRUE.equals(messaging.getMessage().getIsEcho()))
                        ? messaging.getRecipient().getId()
                        : messaging.getSender().getId();

                FacebookMessageType type = classifyMessage(messaging);

                // Skip ECHO messages
                if (type == FacebookMessageType.ECHO) {
                    log.info("🔄 Skipping ECHO message: " + messaging.getMessage().getText());
                    continue;
                }

                // Find tenantId from pageId
                Long tenantId = findTenantIdByPageId(pageId);
                if (tenantId == null) {
                    log.warn("No tenant ID found for page: {}", pageId);
                    continue;
                }
                
                // Set tenant context for thread
                TenantContext.setTenantId(tenantId);
                
                try {
                    // Get connection with tenantId and pageId
                    Optional<FacebookConnection> connectionOpt = connectionRepository.findByTenantIdAndPageId(tenantId, pageId);
                    if (connectionOpt.isEmpty() || !connectionOpt.get().isEnabled()) {
                        log.warn("No or disabled connection found for tenant: {} and page: {}", tenantId, pageId);
                        continue;
                    }
                    FacebookConnection connection = connectionOpt.get();

                    // Process based on message type
                    switch (type) {
                        case TEXT:
                            handleTextMessage(connection, senderId, messaging.getMessage());
                            break;
                        case IMAGE:
                        case VIDEO:
                        case AUDIO:
                        case FILE:
                        case ATTACHMENT: 
                            handleAttachmentMessage(connection, senderId, messaging);
                            break;
                        case QUICK_REPLY:
                            handleQuickReply(connection, senderId, messaging);
                            break;
                        case POSTBACK:
                            handlePostback(connection, senderId, messaging);
                            break;
                        case REACTION:
                            handleReaction(connection, senderId, messaging);
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
                    log.error("Error processing message for page {}: {}", pageId, e.getMessage(), e);
                } finally {
                    // Clear tenant context after processing
                    TenantContext.clear();
                }
            }
        }
    }

    // ========== MESSAGE HANDLERS ==========
    
    private void handleTextMessage(FacebookConnection connection, String senderId, WebhookRequest.Message message) {
        String mid = message.getMid();
        String text = message.getText();
        if (text == null || text.isEmpty() || mid == null) return;

        if (!processedMessageIds.add(mid)) {
            log.info("⚠️ Skipping duplicate message mid=" + mid);
            return;
        }
        
        // Cleanup old message IDs periodically
        cleanupOldMessageIds();

        log.info("✉️ Processing TEXT: " + text);

        // 1️⃣ XÁC ĐỊNH HOẶC TẠO CONVERSATION (following traloitudongV2 pattern)
        UUID connectionId = connection.getId();
        Channel channel = Channel.FACEBOOK;
        var conversation = conversationService.findOrCreate(connectionId, senderId, channel);
        Long conversationId = conversation.getId();
        
        // 2️⃣ LƯU TRỮ VĨNH VIỄN MESSAGE VÀO DB (following traloitudongV2 pattern)
        try {
            messageService.saveMessage(
                conversationId, 
                "user", 
                text, 
                FacebookMessageType.TEXT.name(), 
                Map.of("mid", mid)
            );
            log.info("✅ Đã lưu Message vào DB. Conversation ID: " + conversationId);
        } catch (Exception e) {
            log.error("❌ Lỗi khi lưu Message vào DB: " + e.getMessage());
        }

        // Gửi user message qua WebSocket để hiện trên UX
        try {
            TakeoverMessage userMessage = new TakeoverMessage(
                String.valueOf(conversationId), 
                "user", 
                text, 
                System.currentTimeMillis()
            );
            takeoverService.sendToConversation(userMessage);
            log.info("📡 [Facebook] User message sent via WebSocket. Conversation ID: {}", conversationId);
        } catch (Exception e) {
            log.error("❌ [Facebook] Error sending user message via WebSocket: {}", e.getMessage());
        }

        // Route to PennyBot for processing
        routeToPennyBot(connection, senderId, text, "text", conversationId);
    }
    
    private void handleAttachmentMessage(FacebookConnection connection, String senderId, WebhookRequest.Messaging messaging) {
        String mid = messaging.getMessage().getMid();
        log.info("📎 Processing ATTACHMENT, mid: " + mid);
        
        if (mid == null || !processedMessageIds.add(mid)) {
            log.info("⚠️ Skipping duplicate attachment mid=" + mid);
            return;
        }
        
        // Create conversation for attachment messages
        UUID connectionId = connection.getId();
        Channel channel = Channel.FACEBOOK;
        var conversation = conversationService.findOrCreate(connectionId, senderId, channel);
        Long conversationId = conversation.getId();
        
        messaging.getMessage().getAttachments().forEach(attachment -> {
            String type = attachment.getType(); 
            String url = attachment.getPayload() != null ? attachment.getPayload().getUrl() : null;

            if (url != null) {
                log.info("🖼 ATTACHMENT: type=" + type + ", url=" + url);

                // Save attachment message to DB
                try {
                    messageService.saveMessage(
                        conversationId, 
                        "user", 
                        "[" + type.toUpperCase() + "]", 
                        FacebookMessageType.ATTACHMENT.name(), 
                        Map.of("mid", mid, "url", url, "type", type)
                    );
                    log.info("✅ Đã lưu Attachment Message vào DB. Conversation ID: " + conversationId);
                } catch (Exception e) {
                    log.error("❌ Lỗi khi lưu Attachment Message vào DB: " + e.getMessage());
                }

                String attachmentText = "[" + type.toUpperCase() + "]";
                routeToPennyBot(connection, senderId, attachmentText + " (" + url + ")", type, conversationId);
            }
        });
    }

    private void handleQuickReply(FacebookConnection connection, String senderId, WebhookRequest.Messaging messaging) {
        String payload = messaging.getMessage().getQuickReply().getPayload();
        String text = messaging.getMessage().getText();
        String messageContent = text != null && !text.isEmpty() ? text : payload; 
        String mid = messaging.getMessage().getMid();
        
        // Create conversation for quick reply messages
        UUID connectionId = connection.getId();
        Channel channel = Channel.FACEBOOK;
        var conversation = conversationService.findOrCreate(connectionId, senderId, channel);
        Long conversationId = conversation.getId();
        
        // Save quick reply message to DB
        try {
            messageService.saveMessage(
                conversationId, 
                "user", 
                messageContent, 
                FacebookMessageType.QUICK_REPLY.name(), 
                Map.of("payload", payload, "mid", mid)
            );
            log.info("✅ Đã lưu QuickReply Message vào DB. Conversation ID: " + conversationId);
        } catch (Exception e) {
            log.error("❌ Lỗi khi lưu QuickReply Message vào DB: " + e.getMessage());
        }
        
        log.info("⚡ Processing QUICK_REPLY: " + messageContent);
        routeToPennyBot(connection, senderId, "[QuickReply] " + payload, "quick_reply", conversationId);
    }

    private void handlePostback(FacebookConnection connection, String senderId, WebhookRequest.Messaging messaging) {
        String payload = messaging.getPostback().getPayload();
        String title = messaging.getPostback().getTitle();
        String text = title != null ? title : "[Postback]";
        
        // Create conversation for postback messages
        UUID connectionId = connection.getId();
        Channel channel = Channel.FACEBOOK;
        var conversation = conversationService.findOrCreate(connectionId, senderId, channel);
        Long conversationId = conversation.getId();
        
        // Save postback message to DB
        try {
            messageService.saveMessage(
                conversationId, 
                "user", 
                text, 
                FacebookMessageType.POSTBACK.name(), 
                Map.of("payload", payload)
            );
            log.info("✅ Đã lưu Postback Message vào DB. Conversation ID: " + conversationId);
        } catch (Exception e) {
            log.error("❌ Lỗi khi lưu Postback Message vào DB: " + e.getMessage());
        }
        
        log.info("🔘 Processing POSTBACK: " + text + " (Payload: " + payload + ")");
        routeToPennyBot(connection, senderId, "[Postback] " + payload, "postback", conversationId);
    }
    
    private void handleReaction(FacebookConnection connection, String senderId, WebhookRequest.Messaging messaging) {
        if (messaging.getReaction() == null || messaging.getReaction().getEmoji() == null) {
            log.info("⚠️ Reaction without emoji, skipping.");
            return;
        }

        String action = messaging.getReaction().getAction();
        String emoji = messaging.getReaction().getEmoji();
        String mid = messaging.getReaction().getMid();

        if (mid == null || !processedMessageIds.add(mid)) {
            log.info("⚠️ Skipping duplicate reaction mid=" + mid);
            return;
        }

        log.info("❤️ REACTION: action=" + action + ", emoji=" + emoji);
        // Reactions are system events, don't route to PennyBot
    }

    private void handleRead(WebhookRequest.Messaging messaging) {
        log.info("👀 READ: watermark=" + messaging.getRead().getWatermark());
    }

    private void handleDelivery(WebhookRequest.Messaging messaging) {
        log.info("📬 DELIVERY: mids=" + messaging.getDelivery().getMids());
    }
    
    /**
     * Route message to PennyBot for processing
     * Following traloitudongV2 pattern but using PennyBotManager
     */
    private void routeToPennyBot(FacebookConnection connection, String senderId, String messageText, String messageType, Long conversationId) {
        log.info("🤖 [Penny] Starting message processing...");
        
        // Declare conversation variable at method scope and initialize it early
        var conversation = (Conversation) null;
        
        try {
            // 1️⃣ LƯU TẠM VÀO REDIS (cho luồng Agent/Takeover)
            TakeoverMessage takeoverMessage = new TakeoverMessage(
                String.valueOf(conversationId), 
                "user", 
                messageText, 
                System.currentTimeMillis()
            );
            takeoverService.saveMessage(takeoverMessage);
            log.info("💾 Saved message to Redis for Takeover.");
            
            // 2️⃣ KIỂM TRA LUỒNG: TAKEOVER vs PENNY BOT vs BOTPRESS
            UUID connectionId = connection.getId();
            conversation = conversationService.findOrCreate(connectionId, senderId, Channel.FACEBOOK);
            boolean isTakenOver = conversation.getIsTakenOverByAgent();
            log.info("🔍 [DEBUG] Checking takeover status for conversation {}: {}", conversation.getId(), isTakenOver);
            
            if (isTakenOver) {
                log.info("🛑 Conversation {} is taken over by Agent. Skipping Penny/Botpress processing.", conversationId);
                
                // 3️⃣ Push WebSocket cho Agent đang xem conversation này
                try {
                    takeoverService.sendToConversation(takeoverMessage);
                    log.info("📢 Sent message to Agent via WebSocket.");
                } catch (Exception e) {
                    log.error("❌ Error sending WebSocket to Agent: {}", e.getMessage());
                }
                return; // KHÔNG chuyển đến bot
            }
            
            // 4️⃣ XỬ LÝ QUA PENNY BOT TRƯỚC
            log.info("🤖 [Penny] Routing message to Penny Bot...");
            UUID botId = UUID.fromString(connection.getBotId());
            String pennyResponse = pennyBotManager.processMessage(botId, messageText, connection.getOwnerId(), false);
            
            boolean pennyHandled = pennyResponse != null && !pennyResponse.trim().isEmpty();
            
            if (pennyHandled) {
                log.info("✅ [Penny] Bot handled message. Sending response to Facebook...");
                
                // Lưu message từ bot vào database
                try {
                    messageService.saveMessage(
                        conversation.getId(), 
                        "bot", 
                        pennyResponse, 
                        FacebookMessageType.TEXT.name(), 
                        null
                    );
                    log.info("💾 [Penny] Saved bot response to DB. Conversation ID: {}", conversation.getId());
                } catch (Exception e) {
                    log.error("❌ [Penny] Error saving bot message to DB: {}", e.getMessage());
                }
                
                // Gửi bot message qua WebSocket để hiện trên UX
                try {
                    TakeoverMessage botMessage = new TakeoverMessage(
                        String.valueOf(conversation.getId()), 
                        "bot", 
                        pennyResponse, 
                        System.currentTimeMillis()
                    );
                    takeoverService.sendToConversation(botMessage);
                    log.info("📡 [Penny] Bot message sent via WebSocket. Conversation ID: {}", conversation.getId());
                } catch (Exception e) {
                    log.error("❌ [Penny] Error sending bot message via WebSocket: {}", e.getMessage());
                }
                
                // Gửi response từ Penny Bot đến Facebook user
                facebookMessengerService.sendMessageToUser(connection.getPageId(), senderId, pennyResponse, connection.getPageAccessToken());
                log.info("📤 [Penny] Response sent to Facebook user: {}", pennyResponse);
                return; // Penny xử lý xong, kết thúc luồng
            }
            
            log.info("⏭️ [Penny] Bot didn't handle message. Routing to Botpress...");
            
        } catch (Exception e) {
            log.error("❌ [Penny] Error processing message: {}", e.getMessage(), e);
            log.info("⏭️ [Penny] Error occurred, routing to Botpress as fallback...");
            
            // Ensure conversation is initialized even if Penny processing fails
            if (conversation == null) {
                try {
                    UUID connectionId = connection.getId();
                    conversation = conversationService.findOrCreate(connectionId, senderId, Channel.FACEBOOK);
                    log.info("🔄 [Fallback] Initialized conversation after Penny error: {}", conversation.getId());
                } catch (Exception convEx) {
                    log.error("❌ [Fallback] Failed to initialize conversation: {}", convEx.getMessage());
                    return; // Cannot proceed without conversation
                }
            }
        }

        // 5️⃣ CHUYỂN TIẾP TỚI BOTPRESS (FALLBACK) - CHỈ KHI PROVIDER LÀ BOTPRESS
        if ("BOTPRESS".equals(connection.getChatbotProvider())) {
            log.info("🤖 [Botpress] Routing message to Botpress (fallback)...");
            try {
                log.info("➡️ Routing to {} provider...", connection.getChatbotProvider());
                
                Map<String, Object> chatbotResponse = chatbotServiceWrapper.sendMessage(
                    connection, senderId, messageText
                );
                
                if (chatbotResponse != null) {
                    log.info("🚀 [{}] Received response from bot, sending back to user...", connection.getChatbotProvider());
                    
                    // 💾 Lưu bot response vào database (CHO CẢ BOTPRESS FALLBACK)
                    try {
                        if (conversation != null) {
                            messageService.saveMessage(
                                conversation.getId(), 
                                "bot", 
                                chatbotResponse.toString(), 
                                FacebookMessageType.TEXT.name(), 
                                null
                            );
                            log.info("💾 [{}] Saved bot response to DB. Conversation ID: {}", connection.getChatbotProvider(), conversation.getId());
                        } else {
                            log.warn("⚠️ [{}] Conversation is null, skipping database save", connection.getChatbotProvider());
                        }
                    } catch (Exception e) {
                        log.error("❌ [{}] Error saving bot message to DB: {}", connection.getChatbotProvider(), e.getMessage());
                    }
                    
                    // 📡 Gửi bot response qua WebSocket để hiện trên UX (CHO CẢ BOTPRESS FALLBACK)
                    try {
                        if (conversation != null) {
                            TakeoverMessage botMessage = new TakeoverMessage(
                                String.valueOf(conversation.getId()), 
                                "bot", 
                                chatbotResponse.toString(), 
                                System.currentTimeMillis()
                            );
                            takeoverService.sendToConversation(botMessage);
                            log.info("📡 [{}] Bot message sent via WebSocket. Conversation ID: {}", connection.getChatbotProvider(), conversation.getId());
                        } else {
                            log.warn("⚠️ [{}] Conversation is null, skipping WebSocket send", connection.getChatbotProvider());
                        }
                    } catch (Exception e) {
                        log.error("❌ [{}] Error sending bot message via WebSocket: {}", connection.getChatbotProvider(), e.getMessage());
                    }
                    
                    // Gửi response qua BotpressServiceFb
                    botpressServiceFb.sendMessage(connection.getBotId(), senderId, chatbotResponse.toString());
                } else {
                    log.warn("⚠️ [{}] No response from bot", connection.getChatbotProvider());
                }
            } catch (Exception e) {
                log.error("❌ [{}] Error routing to bot: {}", connection.getChatbotProvider(), e.getMessage());
            }
        } else {
            log.info("⚠️ [Penny] Bot didn't handle message and provider is not BOTPRESS ({}). No fallback available.", connection.getChatbotProvider());
        }
    }
    
    /**
     * Cleanup old message IDs to prevent memory leak
     */
    private void cleanupOldMessageIds() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCleanupTime > CLEANUP_INTERVAL_MS) {
            // For simplicity, we clear all. In production, implement time-based cleanup
            if (processedMessageIds.size() > 10000) { // Only clear if too many
                processedMessageIds.clear();
                log.info("🧹 Cleared processed message IDs to prevent memory leak");
            }
            lastCleanupTime = currentTime;
        }
    }
}
