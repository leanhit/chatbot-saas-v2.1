package com.chatbot.shared.penny.service;

import com.chatbot.shared.penny.context.ConversationContext;
import com.chatbot.shared.penny.dto.request.MiddlewareRequest;
import com.chatbot.shared.penny.routing.dto.IntentAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Intent Analyzer - Phân tích ý định người dùng và trích xuất entities
 */
@Service
@Slf4j
public class IntentAnalyzer {
    
    // Vietnamese intent patterns
    private static final Map<String, List<Pattern>> VIETNAMESE_INTENT_PATTERNS = new HashMap<>();
    
    // Entity patterns
    private static final Map<String, Pattern> ENTITY_PATTERNS = new HashMap<>();
    
    static {
        // Initialize Vietnamese intent patterns
        initializeVietnamesePatterns();
        initializeEntityPatterns();
    }
    
    /**
     * Analyze intent and entities from message
     */
    public IntentAnalysisResult analyze(MiddlewareRequest request, ConversationContext context) {
        String originalMessage = request.getMessage();
        String message = VietnameseTextNormalizer.normalize(originalMessage);
        String unaccentedMessage = VietnameseTextNormalizer.removeAccents(originalMessage);
        
        log.debug("🧠 Analyzing intent for message: {} (unaccented: {})", message, unaccentedMessage);
        
        // Extract entities first from normalized message
        Map<String, Object> entities = extractEntities(message);
        // Also extract from unaccented message to be thorough
        Map<String, Object> unaccentedEntities = extractEntities(unaccentedMessage);
        unaccentedEntities.forEach(entities::putIfAbsent);
        
        // Detect intent
        String primaryIntent = detectPrimaryIntent(message, unaccentedMessage, entities, context);
        double confidence = calculateConfidence(message, unaccentedMessage, primaryIntent, entities);
        
        // Get all possible intents
        List<String> allIntents = detectAllIntents(message, unaccentedMessage, entities);
        
        // Determine message type
        String messageType = determineMessageType(message, unaccentedMessage, entities);
        
        // Calculate complexity
        String complexity = calculateComplexity(message, entities, allIntents);
        
        IntentAnalysisResult result = IntentAnalysisResult.builder()
            .primaryIntent(primaryIntent)
            .confidence(confidence)
            .allIntents(allIntents)
            .entities(entities)
            .messageType(messageType)
            .complexity(complexity)
            .language(request.isVietnamese() ? "vi" : "en")
            .processingTime(System.currentTimeMillis())
            .build();
        
        log.debug("🎯 Intent analysis completed - Intent: {}, Confidence: {}, Entities: {}", 
            primaryIntent, confidence, entities.keySet());
        
        return result;
    }
    
    /**
     * Extract entities from message
     */
    private Map<String, Object> extractEntities(String message) {
        Map<String, Object> entities = new HashMap<>();
        
        // Phone number extraction
        if (message.matches(".*\\b(0|\\+84)[0-9]{9,10}\\b.*")) {
            entities.put("phone_number", extractPhoneNumber(message));
        }
        
        // Email extraction
        if (message.matches(".*[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}.*")) {
            entities.put("email", extractEmail(message));
        }
        
        // Price/money extraction
        if (message.matches(".*[0-9]+[,.]?[0-9]*\\s*(vnđ|đ|d|k|nghìn|nghin|triệu|trieu|tỷ|ty).*")) {
            entities.put("price", extractPrice(message));
        }
        
        // Product extraction
        List<String> products = extractProducts(message);
        if (!products.isEmpty()) {
            entities.put("products", products);
        }
        
        // Order ID extraction
        if (message.matches(".*\\b(order|đơn|don|mã|ma)[#\\s]*([a-zA-Z0-9]+)\\b.*")) {
            entities.put("order_id", extractOrderId(message));
        }
        
        // Location/address extraction
        if (message.matches(".*\\b(địa chỉ|dia chi|tại|tai|ở|o)\\s+[^.!?]+.*")) {
            entities.put("location", extractLocation(message));
        }
        
        // Time/date extraction
        if (message.matches(".*\\b(hôm nay|hom nay|ngày mai|ngay mai|tuần này|tuan nay|tháng này|thang nay)\\b.*")) {
            entities.put("time", extractTime(message));
        }
        
        return entities;
    }
    
    /**
     * Detect primary intent
     */
    private String detectPrimaryIntent(String message, String unaccentedMessage, Map<String, Object> entities, ConversationContext context) {
        // Check for specific intents based on entities first
        if (entities.containsKey("order_id")) {
            return "order_inquiry";
        }
        
        if (entities.containsKey("phone_number") || entities.containsKey("email")) {
            return "contact_info_provided";
        }
        
        if (entities.containsKey("price")) {
            return "price_inquiry";
        }
        
        if (entities.containsKey("products")) {
            return "product_inquiry";
        }
        
        // Check intent patterns
        for (Map.Entry<String, List<Pattern>> entry : VIETNAMESE_INTENT_PATTERNS.entrySet()) {
            String intent = entry.getKey();
            List<Pattern> patterns = entry.getValue();
            
            for (Pattern pattern : patterns) {
                if (pattern.matcher(message).find() || pattern.matcher(unaccentedMessage).find()) {
                    return intent;
                }
            }
        }
        
        // Consider conversation context
        if (context != null && context.getLastIntent() != null) {
            String lastIntent = context.getLastIntent();
            if (isFollowUpMessage(message, lastIntent) || isFollowUpMessage(unaccentedMessage, lastIntent)) {
                return lastIntent + "_followup";
            }
        }
        
        // Default intent
        return "general_chat";
    }
    
    /**
     * Calculate confidence score
     */
    private double calculateConfidence(String message, String unaccentedMessage, String intent, Map<String, Object> entities) {
        double confidence = 0.5; // Base confidence
        
        // Boost confidence based on entities
        if (!entities.isEmpty()) {
            confidence += 0.2 * Math.min(entities.size(), 3) / 3.0;
        }
        
        // Boost confidence based on intent pattern matching
        if (VIETNAMESE_INTENT_PATTERNS.containsKey(intent)) {
            List<Pattern> patterns = VIETNAMESE_INTENT_PATTERNS.get(intent);
            int matchCount = 0;
            for (Pattern pattern : patterns) {
                if (pattern.matcher(message).find() || pattern.matcher(unaccentedMessage).find()) {
                    matchCount++;
                }
            }
            confidence += 0.3 * (matchCount / (double) patterns.size());
        }
        
        // Adjust based on message length
        if (message.length() > 10) {
            confidence += 0.1;
        }
        
        return Math.min(confidence, 1.0);
    }
    
    /**
     * Detect all possible intents
     */
    private List<String> detectAllIntents(String message, String unaccentedMessage, Map<String, Object> entities) {
        List<String> intents = new ArrayList<>();
        
        // Add primary intent
        String primaryIntent = detectPrimaryIntent(message, unaccentedMessage, entities, null);
        intents.add(primaryIntent);
        
        // Add other matching intents
        for (Map.Entry<String, List<Pattern>> entry : VIETNAMESE_INTENT_PATTERNS.entrySet()) {
            String intent = entry.getKey();
            if (!intent.equals(primaryIntent)) {
                List<Pattern> patterns = entry.getValue();
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(message).find() || pattern.matcher(unaccentedMessage).find()) {
                        intents.add(intent);
                        break;
                    }
                }
            }
        }
        
        return intents;
    }
    
    /**
     * Determine message type
     */
    private String determineMessageType(String message, String unaccentedMessage, Map<String, Object> entities) {
        if (entities.containsKey("phone_number") || entities.containsKey("email")) {
            return "contact_info";
        }
        if (entities.containsKey("products") || entities.containsKey("price")) {
            return "product_inquiry";
        }
        if (entities.containsKey("order_id")) {
            return "order_related";
        }
        if (message.contains("?") || message.matches(".*(làm thế nào|cách|như thế nào).*") ||
            unaccentedMessage.matches(".*(lam the nao|cach|nhu the nao).*")) {
            return "question";
        }
        if (message.matches(".*(xin chào|chào|hello|hi).*") ||
            unaccentedMessage.matches(".*(xin chao|chao|hello|hi).*")) {
            return "greeting";
        }
        if (message.matches(".*(cảm ơn|thanks|thank).*") ||
            unaccentedMessage.matches(".*(cam on|thanks|thank).*")) {
            return "gratitude";
        }
        return "general_chat";
    }
    
    /**
     * Calculate complexity level
     */
    private String calculateComplexity(String message, Map<String, Object> entities, List<String> intents) {
        int complexityScore = 0;
        
        // Message length factor
        if (message.length() > 50) complexityScore += 1;
        if (message.length() > 100) complexityScore += 1;
        
        // Entity complexity
        complexityScore += Math.min(entities.size(), 3);
        
        // Intent complexity
        complexityScore += Math.min(intents.size(), 2);
        
        // Question complexity
        if (message.contains("?")) complexityScore += 1;
        
        if (complexityScore <= 2) return "low";
        if (complexityScore <= 4) return "medium";
        return "high";
    }
    
    // Entity extraction helper methods
    
    private String extractPhoneNumber(String message) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b(0|\\+84)[0-9]{9,10}\\b").matcher(message);
        return matcher.find() ? matcher.group() : null;
    }
    
    private String extractEmail(String message) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matcher(message);
        return matcher.find() ? matcher.group() : null;
    }
    
    private String extractPrice(String message) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("[0-9]+[,.]?[0-9]*\\s*(vnđ|đ|d|k|nghìn|nghin|triệu|trieu|tỷ|ty)").matcher(message);
        return matcher.find() ? matcher.group() : null;
    }
    
    private List<String> extractProducts(String message) {
        List<String> products = new ArrayList<>();
        // Common product keywords in Vietnamese (both accented and unaccented)
        String[] productKeywords = {
            "sản phẩm", "san pham", "hàng", "hang", "sách", "sach", 
            "điện thoại", "dien thoai", "laptop", "quần áo", "quan ao", "giày", "giay"
        };
        
        for (String keyword : productKeywords) {
            if (message.contains(keyword)) {
                products.add(keyword);
            }
        }
        
        return products;
    }
    
    private String extractOrderId(String message) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b(order|đơn|don|mã|ma)[#\\s]*([a-zA-Z0-9]+)\\b").matcher(message);
        return matcher.find() ? matcher.group(2) : null;
    }
    
    private String extractLocation(String message) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\b(địa chỉ|dia chi|tại|tai|ở|o)\\s+([^,.!?]+)").matcher(message);
        return matcher.find() ? matcher.group(2).trim() : null;
    }
    
    private String extractTime(String message) {
        if (message.contains("hôm nay") || message.contains("hom nay")) return "hôm nay";
        if (message.contains("ngày mai") || message.contains("ngay mai")) return "ngày mai";
        if (message.contains("tuần này") || message.contains("tuan nay")) return "tuần này";
        if (message.contains("tháng này") || message.contains("thang nay")) return "tháng này";
        return null;
    }
    
    private boolean isFollowUpMessage(String message, String lastIntent) {
        // Simple follow-up detection
        return message.length() < 20 && 
               (message.matches(".*\\b(đúng|vâng|ok|có|dung|vang|co)\\b.*") || 
                message.matches(".*\\b(không|ko|khong)\\b.*"));
    }
    
    // Initialize patterns
    
    private static void initializeVietnamesePatterns() {
        // Order related intents
        VIETNAMESE_INTENT_PATTERNS.put("order_inquiry", Arrays.asList(
            Pattern.compile(".*\\b(kiểm tra|tình trạng|thông tin)\\s+(đơn|order)\\b.*"),
            Pattern.compile(".*\\b(đơn hàng|order)\\s+(của tôi|của mình)\\b.*"),
            Pattern.compile(".*\\b(mã|số)\\s+(đơn|order)\\b.*")
        ));
        
        // Product inquiry intents
        VIETNAMESE_INTENT_PATTERNS.put("product_inquiry", Arrays.asList(
            Pattern.compile(".*\\b(thông tin|chi tiết|giá)\\s+(sản phẩm|sp)\\b.*"),
            Pattern.compile(".*\\b(mua|đặt|order)\\s+(sản phẩm|sp|hàng)\\b.*"),
            Pattern.compile(".*\\b(sản phẩm|sp|hàng)\\s+(này|kia|đó)\\b.*")
        ));
        
        // Price inquiry intents
        VIETNAMESE_INTENT_PATTERNS.put("price_inquiry", Arrays.asList(
            Pattern.compile(".*\\b(giá|giá bao nhiêu|bao nhiêu tiền)\\b.*"),
            Pattern.compile(".*\\b(bao nhiêu|giá)\\s+(tiền|vnđ|đ)\\b.*"),
            Pattern.compile(".*\\b(cost|price)\\b.*")
        ));
        
        // Customer support intents
        VIETNAMESE_INTENT_PATTERNS.put("customer_support", Arrays.asList(
            Pattern.compile(".*\\b(hỗ trợ|trợ giúp|help)\\b.*"),
            Pattern.compile(".*\\b(liên hệ|contact)\\s+(nhân viên|admin|support)\\b.*"),
            Pattern.compile(".*\\b(khó khăn|vấn đề|lỗi)\\b.*")
        ));
        
        // Greeting intents
        VIETNAMESE_INTENT_PATTERNS.put("greeting", Arrays.asList(
            Pattern.compile(".*\\b(chào|xin chào|hello|hi)\\b.*"),
            Pattern.compile(".*\\b(chào buổi sáng|chào buổi chiều|chào buổi tối)\\b.*")
        ));
        
        // Gratitude intents
        VIETNAMESE_INTENT_PATTERNS.put("gratitude", Arrays.asList(
            Pattern.compile(".*\\b(cảm ơn|thanks|thank|cảm on)\\b.*"),
            Pattern.compile(".*\\b(good|tuyệt vời|hay)\\b.*")
        ));
    }
    
    private static void initializeEntityPatterns() {
        ENTITY_PATTERNS.put("phone_number", Pattern.compile("\\b(0|\\+84)[0-9]{9,10}\\b"));
        ENTITY_PATTERNS.put("email", Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"));
        ENTITY_PATTERNS.put("price", Pattern.compile("[0-9]+[,.]?[0-9]*\\s*(vnđ|đ|k|nghìn|triệu|tỷ)"));
        ENTITY_PATTERNS.put("order_id", Pattern.compile("\\b(order|đơn|mã)[#\\s]*([a-zA-Z0-9]+)\\b"));
    }
}
