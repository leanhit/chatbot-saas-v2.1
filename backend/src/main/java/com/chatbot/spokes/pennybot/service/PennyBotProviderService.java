package com.chatbot.spokes.pennybot.service;

import lombok.RequiredArgsConstructor;
import com.chatbot.spokes.facebook.webhook.service.ChatbotProviderService;
import com.chatbot.spokes.pennybot.config.DefaultMessageConfig;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.spokes.facebook.dto.FacebookConnectionDTO;
import com.chatbot.spokes.facebook.service.FacebookConnectionQueryService;
import com.chatbot.core.message.store.service.LLMClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * PennyBot Provider Service Implementation for Facebook integration
 * Implements ChatbotProviderService
 * interface to handle message processing
 * 
 * DECOUPLED: Now uses FacebookConnectionQueryService (DTO-based) instead of direct repository access
 */
@Service("pennyBotProviderService")
@Primary
@RequiredArgsConstructor
@Slf4j
public class PennyBotProviderService implements ChatbotProviderService {

    @Value("${app.integrations.pennybot.api-url:http://localhost:3000}")
    private String pennyBotUrl;

    @Value("${app.integrations.pennybot.api-key:pennybot-key}")
    private String pennyBotApiKey;
    

    private final DefaultMessageConfig messageConfig;
    
    private final ConversationRepository conversationRepository;
    
    // DECOUPLED: Using DTO-based query service instead of direct repository access
    private final FacebookConnectionQueryService facebookConnectionQueryService;

    // LLM Client for Smart AI Fallback
    private final LLMClient llmClient;

    @Override
    public Map<String, Object> sendMessage(String botId, String senderId, String messageText) {
        log.info("Sending message to PennyBot - Bot: {}, Sender: {}, Message: {}", botId, senderId, messageText);
        
        try {
            // Generate bot response
            String responseMessage = generateDefaultResponse(messageText);
            
            // NOTE: Do NOT save to database or send to Facebook here
            // FacebookEventConsumer handles database saving and Facebook sending
            // to avoid duplicate messages
            
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
        
        // Smart LLM Fallback when message doesn't match predefined basic rules
        if (llmClient != null && llmClient.isEnabled()) {
            try {
                String systemPrompt = "Bạn là Penny, trợ lý tư vấn bán hàng chuyên nghiệp. Trả lời người dùng một cách thân thiện, ngắn gọn (tối đa 3-4 câu), lịch sự bằng tiếng Việt. Tránh đưa ra thông tin không chính xác.";
                String aiResponse = llmClient.sendPrompt(systemPrompt, messageText);
                if (aiResponse != null && !aiResponse.trim().isEmpty()) {
                    log.info("🤖 [LLM Smart Fallback] Generated AI response for message: '{}'", messageText);
                    return aiResponse.trim();
                }
            } catch (Exception e) {
                log.error("❌ [LLM Smart Fallback] Error generating response from LLM API: {}", e.getMessage());
            }
        }
        
        // Default fallback if LLM is disabled, unavailable, or failed
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

    /**
     * Check if there are active conversations for a bot
     */
    public boolean checkActiveConversations(String botId) {
        try {
            log.debug("Checking active conversations for bot: {}", botId);
            
            // Query conversation repository for active conversations with this botId
            java.util.List<FacebookConnectionDTO> connections = facebookConnectionQueryService.getActiveConnectionsByBotId(botId);
            
            if (connections.isEmpty()) {
                log.debug("No active connections found for bot: {}", botId);
                return false;
            }
            
            // Check if any connection has recent activity
            java.time.LocalDateTime oneHourAgo = java.time.LocalDateTime.now().minusHours(1);
            
            for (FacebookConnectionDTO connection : connections) {
                java.util.Optional<Conversation> recentConversation = conversationRepository
                    .findByExternalUserIdAndConnectionId("", connection.getId())
                    .stream()
                    .filter(c -> c.getUpdatedAt().isAfter(oneHourAgo))
                    .findFirst();
                
                if (recentConversation.isPresent()) {
                    log.debug("Found active conversation for bot: {}", botId);
                    return true;
                }
            }
            
            log.debug("No active conversations found for bot: {}", botId);
            return false;
            
        } catch (Exception e) {
            log.error("Error checking active conversations for bot {}: {}", botId, e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderType() {
        return "PENNYBOT";
    }
}
