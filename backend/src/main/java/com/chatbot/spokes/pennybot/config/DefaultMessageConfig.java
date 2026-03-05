package com.chatbot.spokes.pennybot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

import java.util.Map;
import java.util.HashMap;

/**
 * Configuration for default messages in PennyBot
 */
@Component
@ConfigurationProperties(prefix = "pennybot.default-messages")
@Data
public class DefaultMessageConfig {
    
    private Map<String, String> greetings = new HashMap<>();
    private Map<String, String> gratitude = new HashMap<>();
    private Map<String, String> goodbye = new HashMap<>();
    private Map<String, String> identity = new HashMap<>();
    private Map<String, String> capabilities = new HashMap<>();
    private Map<String, String> fallback = new HashMap<>();
    
    public DefaultMessageConfig() {
        // Initialize default Vietnamese messages
        greetings.put("vi", "Xin chào! Tôi là PennyBot, trợ lý ảo của bạn. Tôi có thể giúp gì cho bạn ạ?");
        greetings.put("en", "Hello! I'm PennyBot, your virtual assistant. How can I help you today?");
        
        gratitude.put("vi", "Rất vui vì đã giúp được bạn! Còn điều gì tôi có thể hỗ trợ không ạ?");
        gratitude.put("en", "I'm glad I could help! Is there anything else I can assist you with?");
        
        goodbye.put("vi", "Chào bạn và hẹn gặp lại! Nếu cần giúp đỡ, đừng ngần ngại liên hệ nhé.");
        goodbye.put("en", "Goodbye and see you again! If you need help, don't hesitate to contact us.");
        
        identity.put("vi", "Tôi là PennyBot, một trợ lý ảo thông minh được thiết kế để hỗ trợ bạn trong các công việc hàng ngày.");
        identity.put("en", "I'm PennyBot, an intelligent virtual assistant designed to help you with daily tasks.");
        
        capabilities.put("vi", "Tôi có thể giúp bạn với nhiều việc như: hỗ trợ khách hàng, quản lý đơn hàng, trả lời câu hỏi về sản phẩm, và nhiều hơn nữa!");
        capabilities.put("en", "I can help you with many things like: customer support, order management, product inquiries, and much more!");
        
        fallback.put("vi", "Xin lỗi, tôi chưa hiểu ý của bạn. Tôi có thể giúp bạn với các chủ đề về sản phẩm, đơn hàng, hoặc hỗ trợ khách hàng. Bạn có thể nói rõ hơn được không ạ?");
        fallback.put("en", "I'm sorry, I don't understand what you mean. I can help you with topics about products, orders, or customer support. Could you clarify more?");
    }
    
    public String getMessage(String type, String language) {
        Map<String, String> messages = getMessageMap(type);
        if (messages != null) {
            return messages.getOrDefault(language, messages.get("vi")); // Default to Vietnamese
        }
        return fallback.getOrDefault(language, fallback.get("vi"));
    }
    
    private Map<String, String> getMessageMap(String type) {
        switch (type.toLowerCase()) {
            case "greeting":
                return greetings;
            case "gratitude":
                return gratitude;
            case "goodbye":
                return goodbye;
            case "identity":
                return identity;
            case "capabilities":
                return capabilities;
            default:
                return fallback;
        }
    }
}
