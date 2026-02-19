package com.chatbot.modules.facebook.webhook.service;

import com.chatbot.modules.facebook.connection.model.FacebookConnection;
import com.chatbot.modules.facebook.webhook.service.FacebookMessengerService;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.modules.penny.core.PennyMiddlewareEngine;
import com.chatbot.modules.penny.dto.request.MiddlewareRequest;
import com.chatbot.modules.penny.dto.response.MiddlewareResponse;
import com.chatbot.modules.penny.service.PennyBotManager;
import com.chatbot.modules.penny.model.PennyBot;
import com.chatbot.modules.penny.model.PennyBotType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Service tích hợp Penny Bot vào luồng xử lý tin nhắn
 */
@Service
@Slf4j
public class PennyBotIntegrationService {

    private final PennyMiddlewareEngine pennyMiddlewareEngine;
    private final PennyBotManager pennyBotManager;
    private final FacebookMessengerService facebookMessengerService;

    public PennyBotIntegrationService(PennyMiddlewareEngine pennyMiddlewareEngine,
                                    PennyBotManager pennyBotManager,
                                    FacebookMessengerService facebookMessengerService) {
        this.pennyMiddlewareEngine = pennyMiddlewareEngine;
        this.pennyBotManager = pennyBotManager;
        this.facebookMessengerService = facebookMessengerService;
    }

    /**
     * Xử lý tin nhắn qua Penny Bot trước khi chuyển đến Botpress
     * @param connection Facebook connection
     * @param senderId ID người gửi
     * @param messageText Nội dung tin nhắn
     * @param conversation Conversation hiện tại
     * @return true nếu Penny Bot đã xử lý, false nếu cần chuyển đến Botpress
     */
    public boolean processWithPennyBot(FacebookConnection connection, String senderId, 
                                      String messageText, Conversation conversation) {
        
        try {
            log.info("🤖 [Penny] Bắt đầu xử lý tin nhắn qua Penny Bot...");
            
            // 1. Lấy Penny Bot cho connection này
            PennyBot pennyBot = getPennyBotForConnection(connection);
            if (pennyBot == null) {
                log.info("❌ [Penny] Không tìm thấy Penny Bot cho connection. Chuyển đến Botpress.");
                return false;
            }

            // 2. Tạo MiddlewareRequest cho Penny
            MiddlewareRequest pennyRequest = MiddlewareRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .botId(pennyBot.getId().toString())
                .userId(senderId)
                .message(messageText)
                .platform("FACEBOOK")
                .language("vi") // Mặc định tiếng Việt
                .pageId(connection.getPageId())
                .conversationId(conversation.getId().toString())
                .build();

            // 3. Xử lý qua Penny Middleware Engine
            MiddlewareResponse pennyResponse = pennyMiddlewareEngine.processMessage(pennyRequest);
            
            // 4. Kiểm tra Penny Bot có xử lý được không
            if (pennyResponse != null && Boolean.TRUE.equals(pennyResponse.getShouldSendResponse())) {
                log.info("✅ [Penny] Bot đã xử lý tin nhắn. Gửi phản hồi: {}", pennyResponse.getResponse());
                
                // 5. Gửi phản hồi của Penny về cho người dùng
                sendPennyResponseToUser(connection.getPageId(), senderId, pennyResponse);
                
                return true; // Penny đã xử lý, không cần Botpress
            }
            
            log.info("⏭️ [Penny] Bot không có logic xử lý. Chuyển đến Botpress.");
            return false; // Chuyển đến Botpress
            
        } catch (Exception e) {
            log.error("❌ [Penny] Lỗi khi xử lý tin nhắn: {}", e.getMessage(), e);
            return false; // Lỗi thì chuyển đến Botpress như fallback
        }
    }

    /**
     * Lấy Penny Bot cho Facebook connection
     */
    private PennyBot getPennyBotForConnection(FacebookConnection connection) {
        try {
            // 1. Thử lấy bot theo tenant và botpress bot ID
            if (connection.getBotId() != null) {
                PennyBot bot = pennyBotManager.getBotsForCurrentTenant().stream()
                    .filter(b -> b.getBotpressBotId().equals(connection.getBotId()))
                    .filter(b -> b.isActive())
                    .filter(b -> b.isEnabled())
                    .findFirst()
                    .orElse(null);
                
                if (bot != null) {
                    log.debug("🔍 [Penny] Tìm thấy bot theo botpress ID: {}", bot.getBotName());
                    return bot;
                }
            }
            
            // 2. Thử lấy bot theo tenant và type CUSTOMER_SERVICE (mặc định)
            PennyBot defaultBot = pennyBotManager.getBotsForCurrentTenant().stream()
                .filter(b -> b.getBotType() == PennyBotType.CUSTOMER_SERVICE)
                .filter(b -> b.isActive())
                .filter(b -> b.isEnabled())
                .findFirst()
                .orElse(null);
            
            if (defaultBot != null) {
                log.debug("🔍 [Penny] Sử dụng bot CUSTOMER_SERVICE mặc định: {}", defaultBot.getBotName());
                return defaultBot;
            }
            
            // 3. Thử lấy bot active đầu tiên
            return pennyBotManager.getBotsForCurrentTenant().stream()
                .filter(b -> b.isActive())
                .filter(b -> b.isEnabled())
                .findFirst()
                .orElse(null);
                
        } catch (Exception e) {
            log.error("❌ [Penny] Lỗi khi tìm Penny Bot: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Gửi phản hồi của Penny Bot về cho người dùng Facebook
     */
    private void sendPennyResponseToUser(String pageId, String senderId, MiddlewareResponse pennyResponse) {
        try {
            String responseText = pennyResponse.getResponse();
            
            // Gửi tin nhắn text
            facebookMessengerService.sendMessageToUser(pageId, senderId, responseText, "penny");
            
            // Gửi quick replies nếu có
            if (pennyResponse.getQuickReplies() != null && !pennyResponse.getQuickReplies().isEmpty()) {
                // TODO: Implement quick replies cho Penny response
                log.info("📋 [Penny] Có {} quick replies cần gửi", pennyResponse.getQuickReplies().size());
            }
            
            // Gửi attachments nếu có
            if (pennyResponse.getAttachments() != null && !pennyResponse.getAttachments().isEmpty()) {
                // TODO: Implement attachments cho Penny response
                log.info("📎 [Penny] Có {} attachments cần gửi", pennyResponse.getAttachments().size());
            }
            
            log.info("✅ [Penny] Đã gửi phản hồi thành công đến người dùng");
            
        } catch (Exception e) {
            log.error("❌ [Penny] Lỗi khi gửi phản hồi đến người dùng: {}", e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra Penny Bot có sẵn sàng không
     */
    public boolean isPennyBotReady(FacebookConnection connection) {
        try {
            PennyBot pennyBot = getPennyBotForConnection(connection);
            if (pennyBot == null) {
                return false;
            }
            
            // Kiểm tra health của Penny Bot
            Map<String, Object> health = pennyBotManager.getBotHealth(pennyBot.getId());
            return "healthy".equals(health.get("overall"));
            
        } catch (Exception e) {
            log.error("❌ [Penny] Lỗi khi kiểm tra health: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Tự động tạo Penny Bot cho connection nếu chưa có
     */
    public void ensurePennyBotExists(FacebookConnection connection) {
        try {
            if (getPennyBotForConnection(connection) == null) {
                log.info("🔧 [Penny] Tự động tạo Penny Bot cho connection: {}", connection.getPageId());
                
                String ownerId = connection.getOwnerId() != null ? connection.getOwnerId() : "system";
                pennyBotManager.autoCreateBotForConnection(ownerId, connection.getPageId());
                
                log.info("✅ [Penny] Đã tạo Penny Bot thành công");
            }
        } catch (Exception e) {
            log.error("❌ [Penny] Lỗi khi tự động tạo bot: {}", e.getMessage(), e);
        }
    }
}
