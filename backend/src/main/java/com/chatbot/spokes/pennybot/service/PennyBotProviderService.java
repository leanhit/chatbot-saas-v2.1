package com.chatbot.spokes.pennybot.service;

import com.chatbot.spokes.facebook.webhook.service.ChatbotProviderService;
import com.chatbot.spokes.pennybot.config.DefaultMessageConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * PennyBot Provider Service Implementation for Facebook integration
 * Implements ChatbotProviderService interface to handle message processing
 */
@Service("pennyBotProviderService")
@Primary
@Slf4j
public class PennyBotProviderService implements ChatbotProviderService {

    @Value("${app.integrations.pennybot.api-url:http://localhost:3000}")
    private String pennyBotUrl;

    @Value("${app.integrations.pennybot.api-key:pennybot-key}")
    private String pennyBotApiKey;
    
    @Autowired
    private DefaultMessageConfig messageConfig;

    @Override
    public Map<String, Object> sendMessage(String botId, String senderId, String messageText) {
        log.info("Sending message to PennyBot - Bot: {}, Sender: {}, Message: {}", botId, senderId, messageText);
        
        try {
            // Check if message requires default response
            String responseMessage = generateDefaultResponse(messageText);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Message received by PennyBot");
            response.put("botId", botId);
            response.put("senderId", senderId);
            response.put("response", responseMessage);
            response.put("responseType", isDefaultResponse(messageText) ? "default" : "processed");
            response.put("timestamp", System.currentTimeMillis());
            
            return response;
        } catch (Exception e) {
            log.error("Error sending message to PennyBot", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to process message");
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }
    
    /**
     * Check if message requires default response
     */
    private boolean isDefaultResponse(String messageText) {
        String lowerMessage = messageText.toLowerCase().trim();
        
        // Check for common default message patterns
        return lowerMessage.equals("xin chào") || lowerMessage.equals("chào") ||
               lowerMessage.equals("hello") || lowerMessage.equals("hi") ||
               lowerMessage.equals("cảm ơn") || lowerMessage.equals("thanks") ||
               lowerMessage.equals("tạm biệt") || lowerMessage.equals("bye") ||
               lowerMessage.contains("bạn là ai") || lowerMessage.contains("who are you") ||
               lowerMessage.contains("bạn làm gì") || lowerMessage.contains("what do you do") ||
               !containsBusinessKeywords(lowerMessage) && !containsSupportKeywords(lowerMessage);
    }
    
    /**
     * Generate appropriate default response
     */
    private String generateDefaultResponse(String messageText) {
        String lowerMessage = messageText.toLowerCase().trim();
        String language = detectLanguage(messageText);
        
        // Greeting responses
        if (lowerMessage.equals("xin chào") || lowerMessage.equals("chào") || 
            lowerMessage.equals("hello") || lowerMessage.equals("hi")) {
            return messageConfig.getMessage("greeting", language);
        }
        
        // Gratitude responses
        if (lowerMessage.equals("cảm ơn") || lowerMessage.equals("thanks")) {
            return messageConfig.getMessage("gratitude", language);
        }
        
        // Goodbye responses
        if (lowerMessage.equals("tạm biệt") || lowerMessage.equals("bye")) {
            return messageConfig.getMessage("goodbye", language);
        }
        
        // Identity questions
        if (lowerMessage.contains("bạn là ai") || lowerMessage.contains("who are you")) {
            return messageConfig.getMessage("identity", language);
        }
        
        // Capability questions
        if (lowerMessage.contains("bạn làm gì") || lowerMessage.contains("what do you do")) {
            return messageConfig.getMessage("capabilities", language);
        }
        
        // Default fallback
        return messageConfig.getMessage("fallback", language);
    }
    
    /**
     * Detect language from message
     */
    private String detectLanguage(String messageText) {
        String lowerMessage = messageText.toLowerCase();
        
        // Simple language detection based on common words
        if (lowerMessage.contains("xin chào") || lowerMessage.contains("chào") ||
            lowerMessage.contains("cảm ơn") || lowerMessage.contains("tạm biệt") ||
            lowerMessage.contains("bạn là") || lowerMessage.contains("bạn làm")) {
            return "vi";
        }
        
        return "en"; // Default to English
    }
    
    /**
     * Check if message contains business keywords
     */
    private boolean containsBusinessKeywords(String message) {
        return message.contains("đơn hàng") || message.contains("sản phẩm") ||
               message.contains("giá") || message.contains("thanh toán") ||
               message.contains("giao hàng") || message.contains("order") ||
               message.contains("product") || message.contains("price") ||
               message.contains("payment") || message.contains("shipping");
    }
    
    /**
     * Check if message contains support keywords
     */
    private boolean containsSupportKeywords(String message) {
        return message.contains("hỗ trợ") || message.contains("giúp đỡ") ||
               message.contains("lỗi") || message.contains("vấn đề") ||
               message.contains("support") || message.contains("help") ||
               message.contains("error") || message.contains("problem");
    }

    @Override
    public Map<String, Object> sendEvent(String botId, String senderId, String eventName, Map<String, Object> payload) {
        log.info("Sending event to PennyBot - Bot: {}, Sender: {}, Event: {}, Payload: {}", botId, senderId, eventName, payload);
        
        try {
            // TODO: Implement actual PennyBot API call
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Event received by PennyBot");
            response.put("botId", botId);
            response.put("senderId", senderId);
            response.put("eventName", eventName);
            response.put("processedAt", System.currentTimeMillis());
            
            return response;
        } catch (Exception e) {
            log.error("Error sending event to PennyBot", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to process event");
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public boolean healthCheck(String botId) {
        log.info("Checking PennyBot health for bot: {}", botId);
        
        try {
            // TODO: Implement actual PennyBot health check
            // For now, always return true
            return true;
        } catch (Exception e) {
            log.error("Error checking PennyBot health", e);
            return false;
        }
    }

    @Override
    public String getProviderType() {
        return "PENNYBOT";
    }
}
