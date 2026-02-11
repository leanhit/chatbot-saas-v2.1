package com.chatbot.modules.facebook.webhook.service;

import com.chatbot.modules.facebook.connection.model.FacebookConnection;
import com.chatbot.modules.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.modules.facebook.webhook.dto.WebhookRequest;
import com.chatbot.modules.facebook.webhook.model.FacebookMessageType;

import com.chatbot.integrations.odoo.service.CustomerDataService;

import com.chatbot.modules.messaging.messStore.service.ConversationService;
import com.chatbot.modules.messaging.messStore.model.Conversation;
import com.chatbot.modules.messaging.messStore.model.Channel;
import com.chatbot.modules.messaging.messStore.service.MessageService;
import com.chatbot.modules.messaging.takeover.service.TakeoverService;
import com.chatbot.modules.messaging.takeover.model.TakeoverMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Service xử lý các sự kiện webhook từ Facebook.
 * Phiên bản cải tiến: thêm logic Takeover/Handover để kiểm soát luồng Botpress và Agent.
 */
@Slf4j
@Service
public class FacebookWebhookService {

    private final FacebookConnectionRepository connectionRepository;
    private final ChatbotServiceWrapper chatbotServiceWrapper;
    private final FacebookMessengerService facebookMessengerService;
    private final CustomerDataService customerDataService; 

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final TakeoverService takeoverService;
    private final PennyBotIntegrationService pennyBotIntegrationService;

    @Value("${facebook.autoConnect.verifyToken}")
    private String configuredVerifyToken;

    private final Set<String> processedMessageIds = ConcurrentHashMap.newKeySet();
    
    // Cleanup processed message IDs periodically to prevent memory leak
    private static final long CLEANUP_INTERVAL_MS = 60 * 60 * 1000; // 1 hour
    private volatile long lastCleanupTime = System.currentTimeMillis();

    public FacebookWebhookService(FacebookConnectionRepository connectionRepository,
                                  ChatbotServiceWrapper chatbotServiceWrapper,
                                  FacebookMessengerService facebookMessengerService,
                                  CustomerDataService customerDataService, 
                                  ConversationService conversationService, 
                                  MessageService messageService, 
                                  TakeoverService takeoverService,
                                  PennyBotIntegrationService pennyBotIntegrationService) { 
        this.connectionRepository = connectionRepository;
        this.chatbotServiceWrapper = chatbotServiceWrapper;
        this.facebookMessengerService = facebookMessengerService;
        this.customerDataService = customerDataService; 
        
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.takeoverService = takeoverService;
        this.pennyBotIntegrationService = pennyBotIntegrationService;
    }

    public boolean verifyWebhook(String mode, String challenge, String verifyToken) {
        return "subscribe".equals(mode) && configuredVerifyToken.equals(verifyToken);
    }

    // ... (Phần classifyMessage không thay đổi) ...
    private FacebookMessageType classifyMessage(WebhookRequest.Messaging messaging) {
        // ... (Logic phân loại tin nhắn) ...
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
     * Tìm tenantId từ pageId bằng cách query FacebookConnection
     * Ưu tiên connection đang active và enabled
     */
    private Long findTenantIdByPageId(String pageId) {
        // 1. Thử tìm connection active và enabled
        Optional<FacebookConnection> activeConnectionOpt = connectionRepository.findByPageIdForWebhook(pageId);
        if (activeConnectionOpt.isPresent()) {
            FacebookConnection connection = activeConnectionOpt.get();
            log.info("Tìm thấy active connection cho pageId: {} -> tenantId: {}", pageId, connection.getTenantId());
            return connection.getTenantId();
        }
        
        // 2. Nếu không có active connection, log tất cả connections để debug
        List<FacebookConnection> allConnections = connectionRepository.findAllByPageId(pageId);
        if (allConnections.isEmpty()) {
            log.warn("Không tìm thấy FacebookConnection nào cho pageId: {}", pageId);
            return null;
        }
        
        // 3. Log tất cả connections để debug
        log.warn("PageId: {} có {} connections nhưng không có connection nào active:", pageId, allConnections.size());
        for (FacebookConnection conn : allConnections) {
            log.warn("  - TenantId: {}, Enabled: {}, Active: {}, OwnerId: {}", 
                    conn.getTenantId(), conn.isEnabled(), conn.isActive(), conn.getOwnerId());
        }
        
        // 4. Trả về tenant của connection đầu tiên (fallback)
        Long fallbackTenantId = allConnections.get(0).getTenantId();
        log.warn("Sử dụng fallback tenantId: {} cho pageId: {}", fallbackTenantId, pageId);
        return fallbackTenantId;
    }

    @SuppressWarnings("unchecked")
    public void handleWebhookEvent(WebhookRequest request) {
        if (!"page".equals(request.getObject())) return;

        for (WebhookRequest.Entry entry : request.getEntry()) {
            if (entry.getMessaging() == null) continue;

            for (WebhookRequest.Messaging messaging : entry.getMessaging()) {
                // Xác định pageId & senderId
                String pageId = (messaging.getMessage() != null && Boolean.TRUE.equals(messaging.getMessage().getIsEcho()))
                        ? messaging.getSender().getId()
                        : messaging.getRecipient().getId();
                String senderId = (messaging.getMessage() != null && Boolean.TRUE.equals(messaging.getMessage().getIsEcho()))
                        ? messaging.getRecipient().getId()
                        : messaging.getSender().getId();

                FacebookMessageType type = classifyMessage(messaging);

                // Bỏ qua tin nhắn ECHO
                if (type == FacebookMessageType.ECHO) {
                    log.info("🔄 Bỏ qua tin nhắn ECHO: " + messaging.getMessage().getText());
                    continue;
                }

                // Tìm tenantId từ pageId
                Long tenantId = findTenantIdByPageId(pageId);
                if (tenantId == null) {
                    log.warn("Không tìm thấy tenant ID cho page: {}", pageId);
                    continue;
                }
                
                // Thiết lập tenant context cho thread hiện tại
                TenantContext.setTenantId(tenantId);
                
                try {
                    // Lấy kết nối với tenantId và pageId
                    Optional<FacebookConnection> connectionOpt = connectionRepository.findByTenantIdAndPageId(tenantId, pageId);
                    if (connectionOpt.isEmpty() || !connectionOpt.get().isEnabled()) {
                        log.warn("Không tìm thấy hoặc kết nối không được kích hoạt cho tenant: {} và page: {}", tenantId, pageId);
                        continue;
                    }
                    FacebookConnection connection = connectionOpt.get();

                    // Xử lý dựa trên loại message
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
                        log.info("⚠️ Loại message không xác định, bỏ qua.");
                }
                
                } catch (Exception e) {
                    log.error("Lỗi khi xử lý message cho page {}: {}", pageId, e.getMessage(), e);
                } finally {
                    // Clear tenant context sau khi xử lý xong
                    TenantContext.clear();
                }
            }
        }
    }

    // ========== HANDLERS ==========
    private void handleTextMessage(FacebookConnection connection, String senderId, WebhookRequest.Message message) {
        String mid = message.getMid();
        String text = message.getText();
        if (text == null || text.isEmpty() || mid == null) return;

        if (!processedMessageIds.add(mid)) {
            log.info("⚠️ Bỏ qua message trùng mid=" + mid);
            return;
        }
        
        // Cleanup old message IDs periodically
        cleanupOldMessageIds();

        log.info("✉️ Xử lý TEXT: " + text);

        // 1️⃣ XÁC ĐỊNH HOẶC TẠO CONVERSATION
        UUID connectionId = connection.getId();
        Channel channel = Channel.FACEBOOK;
        Conversation conversation = conversationService.findOrCreate(connectionId, senderId, channel);
        Long conversationId = conversation.getId();
        
        // 2️⃣ LƯU TRỮ VĨNH VIỄN MESSAGE VÀO DB
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
            e.printStackTrace();
        }
        
        // 3️⃣ LƯU TẠM VÀO REDIS (cho luồng Agent/Takeover)
        TakeoverMessage takeoverMessage = new TakeoverMessage(
            String.valueOf(conversationId), 
            "user", 
            text, 
            System.currentTimeMillis()
        );
        try {
            takeoverService.saveMessage(takeoverMessage);
            log.info("💾 Đã lưu tạm thời Message vào Redis cho Takeover.");
        } catch (Exception e) {
            log.error("❌ Lỗi khi lưu Message vào Redis: " + e.getMessage());
        }

        // 4️⃣ GỌI DỊCH VỤ ODOO: BẮT NGẦM DỮ LIỆU KHÁCH HÀNG (Logic cũ)
        try {
            log.info("🔍 [OdooMiddleware] Gọi processAndAccumulate...");
            customerDataService.processAndAccumulate(
                    connection.getPageId(),
                    senderId,
                    text
            );
        } catch (Exception e) {
            log.error("❌ [OdooMiddleware] Lỗi khi gọi processAndAccumulate: " + e.getMessage());
            e.printStackTrace();
        }

        // 5️⃣ KIỂM TRA LUỒNG: TAKEOVER vs PENNY BOT vs BOTPRESS
        boolean isTakenOver = conversation.getIsTakenOverByAgent();
        log.info("🔍 [DEBUG] Kiểm tra trạng thái isTakenOverByAgent cho conversation " + conversationId + ": " + isTakenOver);
        
        if (isTakenOver) {
            log.info("🛑 Conversation " + conversationId + " đang được Agent tiếp quản. BỎ QUA Penny Bot & Botpress.");
            
            // 5.1 Push WebSocket cho Agent đang xem conversation này
            try {
                // Sử dụng hàm mới của TakeoverService để gửi WebSocket
                takeoverService.sendToConversation(takeoverMessage);
                log.info("📢 Đã gửi Message mới đến Agent đang xem qua WebSocket.");
            } catch (Exception e) {
                log.error("❌ Lỗi khi gửi WebSocket đến Agent: " + e.getMessage());
            }
            return; // NGẮT LUỒNG
        }

        // 5.2 XỬ LÝ QUA PENNY BOT TRƯỚC
        log.info("🤖 Chuyển tiếp tin nhắn tới Penny Bot trước...");
        try {
            boolean pennyHandled = pennyBotIntegrationService.processWithPennyBot(
                    connection, senderId, text, conversation);
            
            if (pennyHandled) {
                log.info("✅ [Penny] Bot đã xử lý tin nhắn. KHÔNG chuyển đến Botpress.");
                return; // Penny đã xử lý, ngắt luồng
            }
            
            log.info("⏭️ [Penny] Bot không xử lý được. Chuyển đến Botpress...");
            
        } catch (Exception e) {
            log.error("❌ [Penny] Lỗi khi xử lý tin nhắn: " + e.getMessage());
            log.info("⏭️ [Penny] Có lỗi, chuyển đến Botpress như fallback...");
        }

        // 5.3 CHUYỂN TIẾP TỚI BOTPRESS (FALLBACK)
        log.info("🤖 Chuyển tiếp tin nhắn tới Botpress (fallback)...");
        try {
            log.info("➡️ Chuyển tiếp tin nhắn tới " + connection.getChatbotProvider() + ".");
            Map<String, Object> chatbotResponse = chatbotServiceWrapper.sendMessage(
                    connection, senderId, text
            );
            if (chatbotResponse != null) {
                log.info("🚀 [" + connection.getChatbotProvider() + "] Nhận phản hồi từ bot, gửi lại cho người dùng...");
                facebookMessengerService.sendBotpressRepliesToUser(connection.getPageId(), senderId, chatbotResponse);
            } else {
                log.info("⚠️ Không nhận được phản hồi từ " + connection.getChatbotProvider());
            }
        } catch (Exception e) {
            log.error("❌ [" + connection.getChatbotProvider() + "] Lỗi khi gửi hoặc nhận phản hồi: " + e.getMessage());
            e.printStackTrace();
        }
    } 
    
    private void handleAttachmentMessage(FacebookConnection connection, String senderId, WebhookRequest.Messaging messaging) {
        String mid = messaging.getMessage().getMid();
        log.info("📎 Xử lý ATTACHMENT, mid: " + mid);
        
        if (mid == null || !processedMessageIds.add(mid)) {
            log.info("⚠️ Bỏ qua attachment trùng mid=" + mid);
            return;
        }
        
        messaging.getMessage().getAttachments().forEach(attachment -> {
            String type = attachment.getType(); 
            String url = attachment.getPayload() != null ? attachment.getPayload().getUrl() : null;

            if (url != null) {
                log.info("🖼 ATTACHMENT: type=" + type + ", url=" + url);

                // 1️⃣ XÁC ĐỊNH HOẶC TẠO CONVERSATION
                UUID connectionId = connection.getId();
                Channel channel = Channel.FACEBOOK;
                Conversation conversation = conversationService.findOrCreate(connectionId, senderId, channel);
                Long conversationId = conversation.getId();

                String attachmentText = "[" + type.toUpperCase() + "]";
                
                // 2️⃣ LƯU TRỮ VĨNH VIỄN VÀO DB
                try {
                    messageService.saveMessage(
                        conversationId, 
                        "user",
                        attachmentText, 
                        type.toUpperCase(), 
                        Map.of("mid", mid, "url", url)
                    );
                } catch (Exception e) {
                    log.error("❌ Lỗi khi lưu Attachment vào DB: " + e.getMessage());
                }

                // 3️⃣ LƯU TẠM VÀO REDIS (cho luồng Agent/Takeover)
                TakeoverMessage takeoverMessage = new TakeoverMessage(
                    String.valueOf(conversationId), 
                    "user", 
                    attachmentText + (url != null ? " (" + url + ")" : ""), // Nội dung cho UI
                    System.currentTimeMillis()
                );
                try {
                    takeoverService.saveMessage(takeoverMessage);
                } catch (Exception e) {
                    log.error("❌ Lỗi khi lưu Attachment vào Redis: " + e.getMessage());
                }

                // 4️⃣ KIỂM TRA LUỒNG: TAKEOVER vs BOTPRESS
                if (conversation.getIsTakenOverByAgent()) {
                    log.info("🛑 Attachment: Conversation " + conversationId + " đang được Agent tiếp quản. BỎ QUA Botpress.");
                    try {
                        takeoverService.sendToConversation(takeoverMessage);
                    } catch (Exception e) {
                        log.error("❌ Lỗi khi gửi WebSocket Attachment đến Agent: " + e.getMessage());
                    }
                    return; // NGẮT LUỒNG GỬI ĐẾN BOTPRESS
                }
                
                // 4.1 CHUYỂN TIẾP TỚI CHATBOT PROVIDER
                Map<String, Object> eventPayload = new HashMap<>();
                eventPayload.put("type", type);
                eventPayload.put("url", url);

                Map<String, Object> chatbotResponse = chatbotServiceWrapper.sendEvent(
                    connection, senderId, "facebook.attachment", eventPayload
                );
                facebookMessengerService.sendBotpressRepliesToUser(connection.getPageId(), senderId, chatbotResponse);
            }
        });
    }

    private void handleQuickReply(FacebookConnection connection, String senderId, WebhookRequest.Messaging messaging) {
        String payload = messaging.getMessage().getQuickReply().getPayload();
        String text = messaging.getMessage().getText();
        String messageContent = text != null && !text.isEmpty() ? text : payload; 
        
        UUID connectionId = connection.getId();
        Channel channel = Channel.FACEBOOK;
        Conversation conversation = conversationService.findOrCreate(connectionId, senderId, channel);
        Long conversationId = conversation.getId();
        String mid = messaging.getMessage().getMid();

        TakeoverMessage takeoverMessage = new TakeoverMessage(
            String.valueOf(conversationId), 
            "user", 
            messageContent, 
            System.currentTimeMillis()
        );
        
        try {
            messageService.saveMessage(
                conversationId, 
                "user",
                messageContent, 
                FacebookMessageType.QUICK_REPLY.name(), 
                Map.of("payload", payload, "mid", mid)
            );
            takeoverService.saveMessage(takeoverMessage);
        } catch (Exception e) {
            log.error("❌ Lỗi khi lưu QuickReply: " + e.getMessage());
        }

        // KIỂM TRA LUỒNG: TAKEOVER vs BOTPRESS
        if (conversation.getIsTakenOverByAgent()) {
            log.info("🛑 QuickReply: Conversation " + conversationId + " đang được Agent tiếp quản. BỎ QUA Botpress.");
            try {
                takeoverService.sendToConversation(takeoverMessage);
            } catch (Exception e) {
                log.error("❌ Lỗi khi gửi WebSocket QuickReply đến Agent: " + e.getMessage());
            }
            return;
        }

        // LUỒNG CHÍNH: GỬI TIN NHẮN ĐI CHATBOT PROVIDER
        chatbotServiceWrapper.sendMessage(connection, senderId, "[QuickReply] " + payload);
    }

    private void handlePostback(FacebookConnection connection, String senderId, WebhookRequest.Messaging messaging) {
        String payload = messaging.getPostback().getPayload();
        String title = messaging.getPostback().getTitle();
        
        UUID connectionId = connection.getId();
        Channel channel = Channel.FACEBOOK;
        Conversation conversation = conversationService.findOrCreate(connectionId, senderId, channel);
        Long conversationId = conversation.getId();
        String text = title != null ? title : "[Postback]";
        
        TakeoverMessage takeoverMessage = new TakeoverMessage(
            String.valueOf(conversationId), 
            "user", 
            text + " (Payload: " + payload + ")", 
            System.currentTimeMillis()
        );

        try {
            messageService.saveMessage(
                conversationId, 
                "user",
                text, 
                FacebookMessageType.POSTBACK.name(), 
                Map.of("payload", payload)
            );
            takeoverService.saveMessage(takeoverMessage);
        } catch (Exception e) {
            log.error("❌ Lỗi khi lưu Postback: " + e.getMessage());
        }

        // KIỂM TRA LUỒNG: TAKEOVER vs BOTPRESS
        if (conversation.getIsTakenOverByAgent()) {
            log.info("🛑 Postback: Conversation " + conversationId + " đang được Agent tiếp quản. BỎ QUA Botpress.");
             try {
                takeoverService.sendToConversation(takeoverMessage);
            } catch (Exception e) {
                log.error("❌ Lỗi khi gửi WebSocket Postback đến Agent: " + e.getMessage());
            }
            return;
        }

        // LUỒNG CHÍNH: GỬI TIN NHẮN ĐI CHATBOT PROVIDER
        chatbotServiceWrapper.sendMessage(connection, senderId, "[Postback] " + payload);
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

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("action", action);
        eventPayload.put("emoji", emoji);
        eventPayload.put("mid", mid);
        
        // REACTION không cần kiểm tra Takeover vì nó thường là sự kiện hệ thống/không ảnh hưởng luồng hội thoại chính

        try {
            Map<String, Object> chatbotResponse = chatbotServiceWrapper.sendEvent(
                connection, senderId, "facebook.reaction", eventPayload
            );
            facebookMessengerService.sendBotpressRepliesToUser(connection.getPageId(), senderId, chatbotResponse);
        } catch (Exception e) {
            log.error("❌ Error sending reaction event to chatbot: " + e.getMessage());
            processedMessageIds.remove(mid);
        }
    }

    private void handleRead(WebhookRequest.Messaging messaging) {
        log.info("👀 READ: watermark=" + messaging.getRead().getWatermark());
    }

    private void handleDelivery(WebhookRequest.Messaging messaging) {
        log.info("📬 DELIVERY: mids=" + messaging.getDelivery().getMids());
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