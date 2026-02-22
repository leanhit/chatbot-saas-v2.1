package com.chatbot.spokes.facebook.webhook.service;

import lombok.extern.slf4j.Slf4j;

import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.webhook.service.ChatbotProviderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service wrapper để xử lý việc gửi tin nhắn đến chatbot provider phù hợp
 */
@Service
@Slf4j
public class ChatbotServiceWrapper {

    private final ChatbotProviderFactory providerFactory;

    @Autowired
    public ChatbotServiceWrapper(ChatbotProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }

    /**
     * Gửi tin nhắn văn bản đến chatbot và nhận phản hồi
     * @param connection thông tin kết nối Facebook
     * @param senderId ID người gửi
     * @param messageText nội dung tin nhắn
     * @return phản hồi từ chatbot
     */
    public Map<String, Object> sendMessage(FacebookConnection connection, String senderId, String messageText) {
        try {
            ChatbotProviderService provider = providerFactory.getProvider(
                connection.getChatbotProvider().name()
            );
            
            log.info("🔧 Sử dụng provider: " + provider.getProviderType() + 
                             " cho botId: " + connection.getBotId());
            
            return provider.sendMessage(connection.getBotId(), senderId, messageText);
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi tin nhắn qua " + connection.getChatbotProvider() + 
                             ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Gửi tin nhắn từ Agent tới người dùng (sender="agent")
     * @param connection thông tin kết nối Facebook
     * @param senderId ID người gửi
     * @param messageText nội dung tin nhắn
     * @return phản hồi từ chatbot
     */
    public Map<String, Object> sendMessageToUser(FacebookConnection connection, String senderId, String messageText) {
        try {
            ChatbotProviderService provider = providerFactory.getProvider(
                connection.getChatbotProvider().name()
            );
            
            log.info("🔧 Sử dụng provider: " + provider.getProviderType() + 
                             " cho botId: " + connection.getBotId());
            
            return provider.sendMessage(connection.getBotId(), senderId, messageText);
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi tin nhắn qua " + connection.getChatbotProvider() + 
                             ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Gửi sự kiện đến chatbot provider được chỉ định trong connection
     * @param connection thông tin kết nối Facebook
     * @param senderId ID người gửi
     * @param eventName tên sự kiện
     * @param payload dữ liệu đi kèm
     * @return phản hồi từ chatbot
     */
    public Map<String, Object> sendEvent(FacebookConnection connection, String senderId, 
                                       String eventName, Map<String, Object> payload) {
        try {
            ChatbotProviderService provider = providerFactory.getProvider(
                connection.getChatbotProvider().name()
            );
            
            log.info("🔧 Sử dụng provider: " + provider.getProviderType() + 
                             " cho event: " + eventName);
            
            return provider.sendEvent(connection.getBotId(), senderId, eventName, payload);
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi event qua " + connection.getChatbotProvider() + 
                             ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra sức khỏe của chatbot provider
     * @param connection thông tin kết nối Facebook
     * @return true nếu provider khỏe mạnh
     */
    public boolean healthCheck(FacebookConnection connection) {
        try {
            ChatbotProviderService provider = providerFactory.getProvider(
                connection.getChatbotProvider().name()
            );
            
            return provider.healthCheck(connection.getBotId());
        } catch (Exception e) {
            log.error("❌ Health check failed for " + connection.getChatbotProvider() + 
                             ": " + e.getMessage());
            return false;
        }
    }
}
