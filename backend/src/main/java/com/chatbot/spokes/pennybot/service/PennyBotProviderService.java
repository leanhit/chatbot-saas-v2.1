package com.chatbot.spokes.pennybot.service;

import com.chatbot.spokes.facebook.webhook.service.ChatbotProviderService;
import com.chatbot.spokes.pennybot.config.DefaultMessageConfig;
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.messenger.service.FacebookMessengerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private FacebookConnectionRepository facebookConnectionRepository;
    
    @Autowired
    private FacebookMessengerService facebookMessengerService;

    @Override
    public Map<String, Object> sendMessage(String botId, String senderId, String messageText) {
        log.info("Sending message to PennyBot - Bot: {}, Sender: {}, Message: {}", botId, senderId, messageText);
        
        try {
            // Check if message requires default response
            String responseMessage = generateDefaultResponse(messageText);
            
            // Save agent message to database
            saveAgentMessageToDatabase(botId, senderId, messageText);
            
            // Send message to Facebook
            sendMessageToFacebook(botId, senderId, messageText);
            
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
     * Save agent message to database
     */
    private void saveAgentMessageToDatabase(String botId, String senderId, String messageText) {
        try {
            System.out.println("=== DEBUG PENNY BOT SAVING AGENT MESSAGE ===");
            System.out.println("Bot ID: " + botId);
            System.out.println("Sender ID: " + senderId);
            System.out.println("Message: " + messageText);
            
            // Generate unique message ID for idempotency check
            String messageId = "penny_agent_" + botId + "_" + senderId + "_" + messageText.hashCode() + "_" + System.currentTimeMillis();
            System.out.println("Generated Message ID: " + messageId);
            
            // Check if message already exists (idempotency)
            if (messageService.messageExists(messageId)) {
                System.out.println("=== PENNY BOT: Agent message ALREADY EXISTS, skipping save ===");
                log.info("Agent message already exists, skipping save: {}", messageId);
                return;
            }
            
            // Find Facebook connection by botId (botId is String)
            FacebookConnection connection = facebookConnectionRepository.findByBotIdAndIsActiveTrue(botId)
                .orElseThrow(() -> new RuntimeException("Facebook connection not found for botId: " + botId));
            
            // Find conversation by externalUserId and connectionId
            Conversation conversation = conversationRepository.findByExternalUserIdAndConnectionId(senderId, connection.getId())
                .orElseThrow(() -> new RuntimeException("Conversation not found for senderId: " + senderId));
            
            System.out.println("Conversation ID: " + conversation.getId());
            
            // Save agent message
            messageService.saveMessage(
                conversation.getId(),
                "agent",
                messageText,
                "TEXT",
                Map.of("externalMessageId", messageId, "botId", botId, "sentVia", "agent_ui")
            );
            
            System.out.println("=== PENNY BOT: Agent message SAVED to DB ===");
            log.info(" Saved agent message to database. ConversationId: {}, SenderId: {}", conversation.getId(), senderId);
            
        } catch (Exception e) {
            log.error("❌ Failed to save agent message to database: {}", e.getMessage(), e);
            // Don't throw exception to avoid blocking message flow
        }
    }
    
    /**
     * Send message to Facebook user
     */
    private void sendMessageToFacebook(String botId, String senderId, String messageText) {
        try {
            // Find Facebook connection by botId
            FacebookConnection connection = facebookConnectionRepository.findByBotIdAndIsActiveTrue(botId)
                .orElseThrow(() -> new RuntimeException("Facebook connection not found for botId: " + botId));
            
            // Get page access token from connection
            String pageAccessToken = connection.getPageAccessToken();
            
            // Send message via FacebookMessengerService
            facebookMessengerService.sendTextMessage(
                pageAccessToken,
                senderId,
                messageText
            );
            
            log.info("✅ Sent agent message to Facebook. PageId: {}, RecipientId: {}", connection.getPageId(), senderId);
            
        } catch (Exception e) {
            log.error("❌ Failed to send agent message to Facebook: {}", e.getMessage(), e);
            // Don't throw exception to avoid blocking message flow
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
               lowerMessage.contains("giúp đỡ") || lowerMessage.contains("help") ||
               lowerMessage.contains("hỗ trợ") || lowerMessage.contains("support") ||
               lowerMessage.contains("lỗi") || lowerMessage.contains("error") ||
               lowerMessage.contains("vấn đề") || lowerMessage.contains("problem") ||
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
        
        // Help requests
        if (lowerMessage.contains("giúp đỡ") || lowerMessage.contains("help") ||
            lowerMessage.contains("hỗ trợ") || lowerMessage.contains("support")) {
            return "Tôi có thể giúp bạn với các vấn đề sau:\n• 📞 Hỗ trợ kỹ thuật\n• 📦 Theo dõi đơn hàng\n• 🛍️ Tư vấn sản phẩm\n• 💬 Hỗ trợ khách hàng\n• 📊 Báo cáo và thống kê\n\nBạn cần hỗ trợ vấn đề gì ạ?";
        }
        
        // Error reports
        if (lowerMessage.contains("lỗi") || lowerMessage.contains("error") ||
            lowerMessage.contains("vấn đề") || lowerMessage.contains("problem")) {
            return "Tôi rất tiếc khi bạn gặp sự cố. Vui lòng cung cấp thông tin chi tiết:\n• 🐛 Loại lỗi: [mô tả lỗi]\n• 📱 Thiết bị: [browser/device]\n• ⏰ Thời gian xảy ra: [thời gian]\n\nTôi sẽ chuyển đến đội ngũ kỹ thuật để xử lý sớm nhất!";
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
            // Note: Implement actual PennyBot API call to send events
            // This should make HTTP request to PennyBot's event endpoint
            // For now, returning mock response as placeholder
            // Future enhancement: Add actual API integration with PennyBot
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
            // Note: Implement actual PennyBot health check by calling PennyBot API
            // This should verify bot connectivity, status, and responsiveness
            // For now, returning true as placeholder
            // Future enhancement: Add actual API call to PennyBot health endpoint
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
