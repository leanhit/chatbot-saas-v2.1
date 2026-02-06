package com.chatbot.modules.test.service;

import com.chatbot.modules.messagehub.core.dto.MessageRequest;
import com.chatbot.modules.messagehub.core.dto.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Simple Message Test Service - Test luồng tin nhắn không cần Penny
 */
@Service
@Slf4j
public class SimpleMessageTestService {
    
    /**
     * Xử lý tin nhắn test đơn giản
     */
    public MessageResponse processMessage(MessageRequest request) {
        log.info("🧪 Processing test message: {} for conversation: {}", 
                request.getMessage(), request.getConversationId());
        
        String message = request.getMessage().toLowerCase();
        String response;
        
        // Simple intent detection
        if (message.contains("chào") || message.contains("hello") || message.contains("hi")) {
            response = "Chào bạn! Tôi là chatbot test. Tôi có thể giúp gì cho bạn?";
        } else if (message.contains("giúp") || message.contains("help")) {
            response = "Tôi có thể giúp bạn tìm kiếm thông tin sản phẩm và hỗ trợ các câu hỏi thường gặp.";
        } else if (message.contains("tạm biệt") || message.contains("bye")) {
            response = "Tạm biệt! Hẹn gặp lại bạn!";
        } else if (message.contains("sản phẩm") || message.contains("product")) {
            response = "Tôi có thể giúp bạn tìm kiếm sản phẩm. Bạn muốn tìm sản phẩm nào?";
        } else if (message.contains("giá") || message.contains("price")) {
            response = "Để kiểm tra giá, vui lòng cung cấp tên sản phẩm bạn quan tâm.";
        } else {
            response = "Xin lỗi, tôi chưa hiểu yêu cầu của bạn. Bạn có thể diễn đạt lại không?";
        }
        
        return MessageResponse.botProcess(response, Map.of(
                "testMode", true,
                "intent", extractSimpleIntent(message),
                "timestamp", System.currentTimeMillis()
        ));
    }
    
    private String extractSimpleIntent(String message) {
        if (message.contains("chào") || message.contains("hello")) return "greeting";
        if (message.contains("giúp") || message.contains("help")) return "help";
        if (message.contains("tạm biệt") || message.contains("bye")) return "farewell";
        if (message.contains("sản phẩm")) return "product_inquiry";
        if (message.contains("giá")) return "price_inquiry";
        return "unknown";
    }
}
