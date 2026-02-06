package com.chatbot.modules.penny.service;

import com.chatbot.modules.messagehub.core.dto.MessageRequest;
import com.chatbot.modules.messagehub.core.dto.MessageResponse;
import com.chatbot.modules.messagehub.core.model.ConversationContext;
import com.chatbot.modules.penny.model.PennyBot;
import com.chatbot.modules.penny.rules.ResponseTemplate;
import com.chatbot.modules.penny.repository.PennyBotRepository;
import com.chatbot.modules.penny.repository.ResponseTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Penny Message Processor - Tích hợp Penny Bot với Message Hub
 * Xử lý tin nhắn thông qua Penny Bot engine
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PennyMessageProcessor {
    
    private final PennyBotRepository pennyBotRepository;
    private final ResponseTemplateRepository responseTemplateRepository;
    private final PennyBotManager pennyBotManager;
    
    /**
     * Xử lý tin nhắn đến qua Penny Bot
     */
    public MessageResponse processMessage(MessageRequest request, ConversationContext context) {
        log.info("🤖 Penny processing message for conversation: {}, tenant: {}", 
                request.getConversationId(), request.getTenantId());
        
        try {
            // 1. Lấy bot active cho tenant này
        List<PennyBot> activeBots = pennyBotRepository.findByTenantIdAndIsActive(
                request.getTenantId(), true);
        
        if (activeBots.isEmpty()) {
            log.warn("No active Penny bot found for tenant: {}", request.getTenantId());
            return MessageResponse.humanRequired("Không có bot nào được kích hoạt cho tenant này");
        }
        
        PennyBot bot = activeBots.get(0); // Lấy bot đầu tiên trong danh sách
            log.info("Using Penny bot: {} ({})", bot.getBotName(), bot.getId());
            
            // 2. Phân tích intent từ message
            String intent = extractIntent(request.getMessage());
            log.info("Extracted intent: {} from message: {}", intent, request.getMessage());
            
            // 3. Tìm response template phù hợp
            Optional<ResponseTemplate> template = findBestTemplate(bot.getId(), intent, "vi");
            
            if (template.isPresent()) {
                ResponseTemplate responseTemplate = template.get();
                
                // 4. Tạo response từ template
                String responseText = responseTemplate.processTemplate(Map.of(
                        "message", request.getMessage(),
                        "time", java.time.LocalTime.now().toString()
                ));
                
                // 5. Cập nhật usage count
                responseTemplate.incrementUsageCount();
                
                log.info("🎯 Penny bot response: {} for intent: {}", responseText, intent);
                
                return MessageResponse.botProcess(responseText, Map.of(
                                "botId", bot.getId().toString(),
                                "botName", bot.getBotName(),
                                "intent", intent,
                                "templateId", responseTemplate.getId().toString(),
                                "confidence", 0.95
                        ));
            } else {
                // 6. Fallback response khi không có template
                String fallbackResponse = generateFallbackResponse(bot, intent, request.getMessage());
                log.info("🔄 Using fallback response for intent: {}", intent);
                
                return MessageResponse.botProcess(fallbackResponse, Map.of(
                                "botId", bot.getId().toString(),
                                "botName", bot.getBotName(),
                                "intent", intent,
                                "confidence", 0.60,
                                "fallback", true
                        ));
            }
            
        } catch (Exception e) {
            log.error("Error processing message with Penny bot for conversation: {}", 
                    request.getConversationId(), e);
            return MessageResponse.humanRequired("Lỗi xử lý tin nhắn bot: " + e.getMessage());
        }
    }
    
    /**
     * Trích xuất intent từ message
     */
    private String extractIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "unknown";
        }
        
        String lowerMessage = message.toLowerCase().trim();
        
        // Simple intent extraction - có thể nâng cấp với NLP
        if (lowerMessage.contains("chào") || lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
            return "greeting";
        } else if (lowerMessage.contains("tạm biệt") || lowerMessage.contains("bye")) {
            return "farewell";
        } else if (lowerMessage.contains("giúp") || lowerMessage.contains("hỗ trợ")) {
            return "help";
        } else if (lowerMessage.contains("cảm ơn") || lowerMessage.contains("thank")) {
            return "thanks";
        } else if (lowerMessage.contains("sản phẩm") || lowerMessage.contains("mua")) {
            return "product_inquiry";
        } else if (lowerMessage.contains("giá") || lowerMessage.contains("bao nhiêu")) {
            return "price_inquiry";
        } else if (lowerMessage.contains("liên hệ") || lowerMessage.contains("contact")) {
            return "contact";
        } else {
            return "general";
        }
    }
    
    /**
     * Tìm template phù hợp nhất
     */
    private Optional<ResponseTemplate> findBestTemplate(UUID botId, String intent, String language) {
        // Ưu tiên template active theo priority
        List<ResponseTemplate> templates = responseTemplateRepository
                .findByBotIdAndIntentAndIsActiveOrderByPriorityDesc(botId, intent, true);
        
        // Lọc theo language nếu có
        return templates.stream()
                .filter(template -> language.equals(template.getLanguage()) || template.getLanguage().equals("all"))
                .findFirst();
    }
    
    /**
     * Tạo fallback response
     */
    private String generateFallbackResponse(PennyBot bot, String intent, String message) {
        return switch (intent) {
            case "greeting" -> "Chào bạn! Tôi là " + bot.getBotName() + ". Tôi có thể giúp gì cho bạn?";
            case "help" -> "Tôi có thể giúp bạn tìm kiếm sản phẩm, kiểm tra giá, và đặt hàng. Bạn cần giúp gì?";
            case "product_inquiry" -> "Tôi sẽ giúp bạn tìm sản phẩm phù hợp. Bạn có thể cho biết thêm chi tiết không?";
            case "price_inquiry" -> "Để kiểm tra giá, vui lòng cung cấp tên sản phẩm bạn quan tâm.";
            case "contact" -> "Bạn có thể liên hệ qua hotline hoặc email. Tôi sẽ chuyển yêu cầu của bạn đến nhân viên hỗ trợ.";
            default -> "Xin lỗi, tôi chưa hiểu yêu cầu của bạn. Bạn có thể diễn đạt lại không?";
        };
    }
    
}
