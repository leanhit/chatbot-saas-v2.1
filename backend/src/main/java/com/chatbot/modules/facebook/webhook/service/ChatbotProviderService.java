package com.chatbot.modules.facebook.webhook.service;

import java.util.Map;

/**
 * Interface chung cho các nhà cung cấp chatbot (Botpress, Rasa, v.v.)
 */
public interface ChatbotProviderService {
    
    /**
     * Gửi tin nhắn văn bản đến chatbot và nhận phản hồi
     * @param botId ID của bot
     * @param senderId ID của người gửi
     * @param messageText nội dung tin nhắn
     * @return phản hồi từ chatbot
     */
    Map<String, Object> sendMessage(String botId, String senderId, String messageText);
    
    /**
     * Gửi sự kiện/custom payload đến chatbot
     * @param botId ID của bot
     * @param senderId ID của người gửi
     * @param eventName tên sự kiện
     * @param payload dữ liệu đi kèm
     * @return phản hồi từ chatbot
     */
    Map<String, Object> sendEvent(String botId, String senderId, String eventName, Map<String, Object> payload);
    
    /**
     * Kiểm tra kết nối đến chatbot
     * @param botId ID của bot
     * @return true nếu kết nối thành công
     */
    boolean healthCheck(String botId);
    
    /**
     * Lấy loại nhà cung cấp (BOTPRESS, RASA, v.v.)
     * @return tên nhà cung cấp
     */
    String getProviderType();
}
