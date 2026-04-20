package com.chatbot.spokes.pennybot.service;

import com.chatbot.spokes.facebook.webhook.service.ChatbotProviderService;
import com.chatbot.spokes.pennybot.config.DefaultMessageConfig;
import com.chatbot.spokes.pennybot.config.EnhancedDefaultMessageConfig;
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.messenger.service.FacebookMessengerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enhanced PennyBot Provider Service with comprehensive default responses
 */
@Service("enhancedPennyBotProviderService")
@Primary
@ConditionalOnProperty(prefix = "pennybot.default-messages", name = "enhanced", havingValue = "true")
@Slf4j
public class EnhancedPennyBotProviderService implements ChatbotProviderService {

    @Value("${app.integrations.pennybot.api-url:http://localhost:3000}")
    private String pennyBotUrl;

    @Value("${app.integrations.pennybot.api-key:pennybot-key}")
    private String pennyBotApiKey;

    @Autowired
    private DefaultMessageConfig messageConfig;

    @Autowired(required = false)
    private EnhancedDefaultMessageConfig enhancedMessageConfig;

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
        log.info("🚀 Enhanced PennyBot - Bot: {}, Sender: {}, Message: {}", botId, senderId, messageText);
        
        try {
            // Check if message requires default response
            String responseMessage = generateEnhancedDefaultResponse(messageText);
            
            // Save agent message to database
            saveAgentMessageToDatabase(botId, senderId, messageText);
            
            // Send message to Facebook
            sendMessageToFacebook(botId, senderId, messageText);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Message processed by Enhanced PennyBot");
            response.put("botId", botId);
            response.put("senderId", senderId);
            response.put("response", responseMessage);
            response.put("responseType", isEnhancedDefaultResponse(messageText) ? "enhanced-default" : "processed");
            response.put("timestamp", System.currentTimeMillis());
            response.put("provider", "EnhancedPennyBot");
            
            return response;
        } catch (Exception e) {
            log.error("❌ Error sending message to Enhanced PennyBot", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to process message");
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Check if message requires enhanced default response
     */
    private boolean isEnhancedDefaultResponse(String messageText) {
        String lowerMessage = messageText.toLowerCase().trim();
        
        return containsGreeting(lowerMessage) ||
               containsGratitude(lowerMessage) ||
               containsGoodbye(lowerMessage) ||
               containsIdentityQuestion(lowerMessage) ||
               containsCapabilityQuestion(lowerMessage) ||
               containsHelpRequest(lowerMessage) ||
               containsErrorReport(lowerMessage) ||
               containsStatusInquiry(lowerMessage) ||
               containsPricingInquiry(lowerMessage) ||
               !containsBusinessKeywords(lowerMessage) && !containsSupportKeywords(lowerMessage);
    }

    /**
     * Generate enhanced default response based on message content
     */
    private String generateEnhancedDefaultResponse(String messageText) {
        String lowerMessage = messageText.toLowerCase().trim();
        String language = detectLanguage(messageText);
        
        log.info("🧠 Generating enhanced response for: {} (language: {})", lowerMessage, language);
        
        // Enhanced greeting responses
        if (containsGreeting(lowerMessage)) {
            if (enhancedMessageConfig != null) {
                return enhancedMessageConfig.getMessage("greeting", language);
            }
            return messageConfig.getMessage("greeting", language);
        }
        
        // Enhanced gratitude responses
        if (containsGratitude(lowerMessage)) {
            if (enhancedMessageConfig != null) {
                return enhancedMessageConfig.getMessage("gratitude", language);
            }
            return messageConfig.getMessage("gratitude", language);
        }
        
        // Enhanced goodbye responses
        if (containsGoodbye(lowerMessage)) {
            if (enhancedMessageConfig != null) {
                return enhancedMessageConfig.getMessage("goodbye", language);
            }
            return messageConfig.getMessage("goodbye", language);
        }
        
        // Enhanced identity questions
        if (containsIdentityQuestion(lowerMessage)) {
            if (enhancedMessageConfig != null) {
                return enhancedMessageConfig.getMessage("identity", language);
            }
            return messageConfig.getMessage("identity", language);
        }
        
        // Enhanced capability questions
        if (containsCapabilityQuestion(lowerMessage)) {
            if (enhancedMessageConfig != null) {
                return enhancedMessageConfig.getMessage("capabilities", language);
            }
            return messageConfig.getMessage("capabilities", language);
        }
        
        // Enhanced help requests
        if (containsHelpRequest(lowerMessage)) {
            if (enhancedMessageConfig != null) {
                return enhancedMessageConfig.getMessage("help", language);
            }
            return getHelpResponse(language);
        }
        
        // Enhanced error reports
        if (containsErrorReport(lowerMessage)) {
            if (enhancedMessageConfig != null) {
                return enhancedMessageConfig.getMessage("error", language);
            }
            return getErrorResponse(language);
        }
        
        // Status inquiries
        if (containsStatusInquiry(lowerMessage)) {
            if (enhancedMessageConfig != null) {
                return enhancedMessageConfig.getMessage("status", language);
            }
            return getStatusResponse(language);
        }
        
        // Pricing inquiries
        if (containsPricingInquiry(lowerMessage)) {
            if (enhancedMessageConfig != null) {
                return enhancedMessageConfig.getMessage("pricing", language);
            }
            return getPricingResponse(language);
        }
        
        // Enhanced fallback
        if (enhancedMessageConfig != null) {
            return enhancedMessageConfig.getMessage("fallback", language);
        }
        return messageConfig.getMessage("fallback", language);
    }

    // Enhanced detection methods
    private boolean containsGreeting(String message) {
        return message.equals("xin chào") || message.equals("chào") || 
               message.equals("hello") || message.equals("hi") ||
               message.equals("chào buổi sáng") || message.equals("good morning") ||
               message.equals("chào buổi trưa") || message.equals("good afternoon") ||
               message.equals("chào buổi tối") || message.equals("good evening") ||
               message.contains("lâu rồi") || message.contains("dạo đây");
    }

    private boolean containsGratitude(String message) {
        return message.equals("cảm ơn") || message.equals("thanks") ||
               message.equals("cảm ơn nhiều") || message.equals("thanks a lot") ||
               message.contains("tiện") || message.contains("giúp") && message.contains("nhé");
    }

    private boolean containsGoodbye(String message) {
        return message.equals("tạm biệt") || message.equals("bye") ||
               message.equals("chào tạm biệt") || message.equals("goodbye") ||
               message.contains("hẹn gặp lại") || message.contains("see you");
    }

    private boolean containsIdentityQuestion(String message) {
        return message.contains("bạn là ai") || message.contains("who are you") ||
               message.contains("bạn là gì") || message.contains("what are you") ||
               message.contains("giới thiệu") || message.contains("introduce");
    }

    private boolean containsCapabilityQuestion(String message) {
        return message.contains("bạn làm gì") || message.contains("what do you do") ||
               message.contains("làm được gì") || message.contains("what can you do") ||
               message.contains("khả năng") || message.contains("features");
    }

    private boolean containsHelpRequest(String message) {
        return message.contains("giúp đỡ") || message.contains("help") ||
               message.contains("hỗ trợ") || message.contains("support") ||
               message.contains("hướng dẫn") || message.contains("how to");
    }

    private boolean containsErrorReport(String message) {
        return message.contains("lỗi") || message.contains("error") ||
               message.contains("vấn đề") || message.contains("problem") ||
               message.contains("hỏng") || message.contains("bug") ||
               message.contains("không hoạt") || message.contains("not working");
    }

    private boolean containsStatusInquiry(String message) {
        return message.contains("trạng thái") || message.contains("status") ||
               message.contains("hoạt động") || message.contains("operational") ||
               message.contains("báo cáo") || message.contains("report") ||
               message.contains("thống kê") || message.contains("statistics");
    }

    private boolean containsPricingInquiry(String message) {
        return message.contains("giá") || message.contains("price") ||
               message.contains("gói") || message.contains("package") ||
               message.contains("chi phí") || message.contains("cost") ||
               message.contains("báo giá") || message.contains("quotation");
    }

    // Enhanced response generators
    private String getHelpResponse(String language) {
        if ("vi".equals(language)) {
            return "🆘 **Tôi luôn sẵn sàng giúp bạn!**\n\n" +
                   "**🛠️ Hỗ trợ kỹ thuật**\n" +
                   "• Kiểm tra và sửa lỗi hệ thống\n" +
                   "• Hướng dẫn sử dụng tính năng\n" +
                   "• Tối ưu hóa hiệu suất\n\n" +
                   "**📞 Hỗ trợ khách hàng**\n" +
                   "• Giải đáp thắc mắc sản phẩm\n" +
                   "• Hướng dẫn đặt hàng\n" +
                   "• Xử lý khiếu nại và đổi trả\n\n" +
                   "**📊 Thống kê và báo cáo**\n" +
                   "• Xuất báo cáo doanh thu\n" +
                   "• Phân tích dữ liệu sử dụng\n" +
                   "• Theo dõi hiệu suất dịch vụ\n\n" +
                   "Bạn cần hỗ trợ vấn đề nào ạ? 🤔";
        }
        return "🆘 **I'm always here to help you!**\n\n" +
               "**🛠️ Technical Support**\n" +
               "• System troubleshooting and fixes\n" +
               "• Feature usage guidance\n" +
               "• Performance optimization\n\n" +
               "**📞 Customer Support**\n" +
               "• Product inquiries\n" +
               "• Order assistance\n" +
               "• Returns and exchanges\n\n" +
               "**📊 Analytics and Reports**\n" +
               "• Revenue reports\n" +
               "• Usage data analysis\n" +
               "• Service performance tracking\n\n" +
               "What kind of support do you need? 🤔";
    }

    private String getErrorResponse(String language) {
        if ("vi".equals(language)) {
            return "⚠️ **Đã xảy ra lỗi!** Tôi xin lỗi vì sự bất tiện này.\n\n" +
                   "**🔧 Các bước khắc phục:**\n" +
                   "1️⃣ Kiểm tra lại kết nối mạng\n" +
                   "2️⃣ Làm mới trang web (F5)\n" +
                   "3️⃣ Thử lại sau vài phút\n\n" +
                   "**📞 Nếu lỗi vẫn tiếp diễn, vui lòng:**\n" +
                   "• Gọi hotline: 1900-xxxx\n" +
                   "• Email: support@example.com\n" +
                   "• Chat trực tuyến với đội ngũ hỗ trợ\n\n" +
                   "Cảm ơn sự kiên nhẫn của bạn! 😊";
        }
        return "⚠️ **An error has occurred!** I apologize for this inconvenience.\n\n" +
               "**🔧 Troubleshooting steps:**\n" +
               "1️⃣ Check your internet connection\n" +
               "2️⃣ Refresh the webpage (F5)\n" +
               "3️⃣ Try again in a few minutes\n\n" +
               "**📞 If the error persists, please:**\n" +
               "• Call hotline: 1900-xxxx\n" +
               "• Email: support@example.com\n" +
               "• Live chat with our support team\n\n" +
               "Thank you for your patience! 😊";
    }

    private String getStatusResponse(String language) {
        if ("vi".equals(language)) {
            return "📊 **Trạng thái hệ thống hiện tại:**\n\n" +
                   "✅ **Dịch vụ core**: Hoạt động bình thường\n" +
                   "✅ **CSDL**: Kết nối ổn định\n" +
                   "✅ **API**: Phản hồi < 100ms\n" +
                   "✅ **Chatbot**: Sẵn sàng 24/7\n" +
                   "⚠️ **Bảo trì định kỳ**: 2:00-3:00 sáng hàng ngày\n\n" +
                   "Mọi dịch vụ đều hoạt động tốt! 🟢";
        }
        return "📊 **Current System Status:**\n\n" +
               "✅ **Core services**: Operating normally\n" +
               "✅ **Database**: Stable connection\n" +
               "✅ **API**: Response time < 100ms\n" +
               "✅ **Chatbot**: Available 24/7\n" +
               "⚠️ **Scheduled maintenance**: 2:00-3:00 AM daily\n\n" +
               "All services are running well! 🟢";
    }

    private String getPricingResponse(String language) {
        if ("vi".equals(language)) {
            return "💰 **Thông tin giá và gói dịch vụ:**\n\n" +
                   "**🔥 Gói Basic (99k/tháng)**\n" +
                   "• 500 tin nhắn/tháng\n" +
                   "• Hỗ trợ Facebook, Website\n" +
                   "• Báo cáo cơ bản\n\n" +
                   "**⚡ Gói Pro (299k/tháng)**\n" +
                   "• 2.000 tin nhắn/tháng\n" +
                   "• Hỗ trợ đa kênh (FB, Web, Zalo)\n" +
                   "• Báo cáo nâng cao\n" +
                   "• API access\n\n" +
                   "**🚀 Gói Enterprise (599k/tháng)**\n" +
                   "• Không giới hạn tin nhắn\n" +
                   "• Hỗ trợ 24/7 qua điện thoại\n" +
                   "• Tùy chỉnh chatbot\n" +
                   "• Dedicated account manager\n\n" +
                   "Liên hệ sales@example.com để được tư vấn chi tiết! 📞";
        }
        return "💰 **Pricing and Service Plans:**\n\n" +
               "**🔥 Basic Plan ($99/month)**\n" +
               "• 500 messages/month\n" +
               "• Facebook, Website support\n" +
               "• Basic reporting\n\n" +
               "**⚡ Pro Plan ($299/month)**\n" +
               "• 2,000 messages/month\n" +
               "• Multi-channel support (FB, Web, Zalo)\n" +
               "• Advanced reporting\n" +
               "• API access\n\n" +
               "**🚀 Enterprise Plan ($599/month)**\n" +
               "• Unlimited messages\n" +
               "• 24/7 phone support\n" +
               "• Custom chatbot\n" +
               "• Dedicated account manager\n\n" +
               "Contact sales@example.com for detailed consultation! 📞";
    }

    // Rest of the methods remain the same as original...
    private void saveAgentMessageToDatabase(String botId, String senderId, String messageText) {
        try {
            System.out.println("=== DEBUG ENHANCED PENNY BOT SAVING AGENT MESSAGE ===");
            System.out.println("Bot ID: " + botId);
            System.out.println("Sender ID: " + senderId);
            System.out.println("Message: " + messageText);
            
            // Generate unique message ID for idempotency check
            String messageId = "enhanced_agent_" + botId + "_" + senderId + "_" + messageText.hashCode() + "_" + System.currentTimeMillis();
            System.out.println("Generated Message ID: " + messageId);
            
            // Check if message already exists (idempotency)
            if (messageService.messageExists(messageId)) {
                System.out.println("=== ENHANCED PENNY BOT: Agent message ALREADY EXISTS, skipping save ===");
                log.info("Agent message already exists, skipping save: {}", messageId);
                return;
            }
            
            FacebookConnection connection = facebookConnectionRepository.findByBotIdAndIsActiveTrue(botId)
                .orElseThrow(() -> new RuntimeException("Facebook connection not found for botId: " + botId));
            
            Conversation conversation = conversationRepository.findByExternalUserIdAndConnectionId(senderId, connection.getId())
                .orElseThrow(() -> new RuntimeException("Conversation not found for senderId: " + senderId));
            
            System.out.println("Conversation ID: " + conversation.getId());
            
            messageService.saveMessage(
                conversation.getId(),
                "agent",
                messageText,
                "TEXT",
                Map.of("externalMessageId", messageId, "botId", botId, "sentVia", "enhanced_agent_ui", "provider", "EnhancedPennyBot")
            );
            
            System.out.println("=== ENHANCED PENNY BOT: Agent message SAVED to DB ===");
            log.info(" Saved enhanced agent message to database. ConversationId: {}, SenderId: {}", conversation.getId(), senderId);
            
        } catch (Exception e) {
            log.error("❌ Failed to save enhanced agent message to database: {}", e.getMessage(), e);
        }
    }

    private void sendMessageToFacebook(String botId, String senderId, String messageText) {
        try {
            FacebookConnection connection = facebookConnectionRepository.findByBotIdAndIsActiveTrue(botId)
                .orElseThrow(() -> new RuntimeException("Facebook connection not found for botId: " + botId));
            
            String pageAccessToken = connection.getPageAccessToken();
            
            facebookMessengerService.sendTextMessage(
                pageAccessToken,
                senderId,
                messageText
            );
            
            log.info("✅ Sent enhanced message to Facebook. PageId: {}, RecipientId: {}", connection.getPageId(), senderId);
            
        } catch (Exception e) {
            log.error("❌ Failed to send enhanced message to Facebook: {}", e.getMessage(), e);
        }
    }

    private String detectLanguage(String messageText) {
        String lowerMessage = messageText.toLowerCase();
        
        // Enhanced language detection
        if (lowerMessage.contains("xin chào") || lowerMessage.contains("chào") ||
            lowerMessage.contains("cảm ơn") || lowerMessage.contains("tạm biệt") ||
            lowerMessage.contains("bạn là") || lowerMessage.contains("bạn làm") ||
            lowerMessage.contains("giúp đỡ") || lowerMessage.contains("hỗ trợ")) {
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
        log.info("🚀 Enhanced PennyBot - Bot: {}, Sender: {}, Event: {}, Payload: {}", botId, senderId, eventName, payload);
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Event received by Enhanced PennyBot");
            response.put("botId", botId);
            response.put("senderId", senderId);
            response.put("eventName", eventName);
            response.put("processedAt", System.currentTimeMillis());
            response.put("provider", "EnhancedPennyBot");
            
            return response;
        } catch (Exception e) {
            log.error("Error sending event to Enhanced PennyBot", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to process event");
            errorResponse.put("error", e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public boolean healthCheck(String botId) {
        log.info("🏥 Checking Enhanced PennyBot health for bot: {}", botId);
        
        try {
            // TODO: Implement actual Enhanced PennyBot health check
            return true;
        } catch (Exception e) {
            log.error("Error checking Enhanced PennyBot health", e);
            return false;
        }
    }

    @Override
    public String getProviderType() {
        return "ENHANCED_PENNYBOT";
    }
}
