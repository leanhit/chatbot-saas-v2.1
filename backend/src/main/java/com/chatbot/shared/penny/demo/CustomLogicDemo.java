package com.chatbot.shared.penny.demo;

import com.chatbot.shared.penny.context.ConversationContext;
import com.chatbot.shared.penny.dto.request.MiddlewareRequest;
import com.chatbot.shared.penny.dto.response.MiddlewareResponse;
import com.chatbot.shared.penny.rules.*;
import com.chatbot.shared.penny.routing.dto.IntentAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Demo Custom Logic Engine
 * Chạy với: --spring.profiles.active=demo --penny.demo.enabled=true
 */
@Component
@ConditionalOnProperty(name = "penny.demo.enabled", havingValue = "true")
@Slf4j
public class CustomLogicDemo implements CommandLineRunner {

    @Autowired
    private CustomLogicEngine customLogicEngine;

    @Autowired
    private BotRuleManager botRuleManager;

    @Autowired
    private ResponseTemplateManager responseTemplateManager;

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Starting Penny Custom Logic Demo...");
        
        UUID botId = UUID.randomUUID();
        log.info("📱 Created demo bot ID: {}", botId);
        
        // Demo 1: Tạo Rule cho Customer Service
        demoRuleCreation(botId);
        
        // Demo 2: Tạo Template cho Greeting
        demoTemplateCreation(botId);
        
        // Demo 3: Test Custom Logic Processing
        demoCustomLogicProcessing(botId);
        
        // Demo 4: Test Statistics
        demoStatistics(botId);
        
        log.info("✅ Penny Custom Logic Demo completed successfully!");
    }
    
    private void demoRuleCreation(UUID botId) {
        log.info("📝 Demo 1: Creating Custom Rules...");
        
        try {
            // Rule 1: Greeting Rule
            BotRule greetingRule = botRuleManager.createRule(
                botId,
                "greeting_rule",
                "Phản hồi khi user chào hỏi",
                "intent == 'greeting'",
                "{\"text\": \"Xin chào {{user_name}}! Tôi là assistant của Penny Bot. Tôi có thể giúp gì cho bạn?\"}",
                BotRule.RuleType.RESPONSE,
                BotRule.TriggerType.INTENT,
                "greeting",
                10,
                "demo-user"
            );
            log.info("✅ Created greeting rule: {}", greetingRule.getName());
            
            // Rule 2: Order Inquiry Rule
            BotRule orderRule = botRuleManager.createRule(
                botId,
                "order_inquiry_rule",
                "Xử lý khi hỏi về đơn hàng",
                "intent == 'order_inquiry'",
                "{\"text\": \"Để kiểm tra đơn hàng, vui lòng cung cấp mã đơn hàng của bạn. Định dạng: DHxxxxx\"}",
                BotRule.RuleType.RESPONSE,
                BotRule.TriggerType.INTENT,
                "order_inquiry",
                8,
                "demo-user"
            );
            log.info("✅ Created order inquiry rule: {}", orderRule.getName());
            
            // Rule 3: Keyword Rule cho "giúp đỡ"
            BotRule helpRule = botRuleManager.createRule(
                botId,
                "help_keyword_rule",
                "Phản hồi khi user nói giúp đỡ",
                "message.toLowerCase().contains('giúp đỡ')",
                "{\"text\": \"Tôi có thể giúp bạn với các vấn đề sau:\\n1. Kiểm tra đơn hàng\\n2. Hỗ trợ sản phẩm\\n3. Thông tin khuyến mãi\\nBạn cần hỗ trợ gì?\"}",
                BotRule.RuleType.RESPONSE,
                BotRule.TriggerType.KEYWORD,
                "giúp đỡ",
                5,
                "demo-user"
            );
            log.info("✅ Created help keyword rule: {}", helpRule.getName());
            
        } catch (Exception e) {
            log.error("❌ Error creating rules: {}", e.getMessage(), e);
        }
    }
    
    private void demoTemplateCreation(UUID botId) {
        log.info("📋 Demo 2: Creating Response Templates...");
        
        try {
            // Template 1: Greeting Template với Quick Replies
            ResponseTemplate greetingTemplate = responseTemplateManager.createTemplate(
                botId,
                "greeting_template",
                "Template chào hỏi với quick replies",
                "greeting",
                "Chào {{user_name}}! 👋\\n\\nTôi là Penny Bot, rất vui được phục vụ bạn. Tôi có thể giúp:\\n• 📦 Kiểm tra đơn hàng\\n• 🛍️ Tư vấn sản phẩm\\n• 💬 Hỗ trợ khách hàng\\n• 🎁 Thông tin khuyến mãi\\n\\nBạn cần hỗ trợ gì hôm nay?",
                ResponseTemplate.TemplateType.TEXT,
                "vi",
                7,
                null,
                java.util.List.of(
                    Map.of("title", "Kiểm tra đơn hàng", "payload", "check_order"),
                    Map.of("title", "Tư vấn sản phẩm", "payload", "product_advice"),
                    Map.of("title", "Hỗ trợ khách hàng", "payload", "customer_support"),
                    Map.of("title", "Thông tin khuyến mãi", "payload", "promotions")
                ),
                "demo-user"
            );
            log.info("✅ Created greeting template: {}", greetingTemplate.getName());
            
            // Template 2: Order Status Template
            ResponseTemplate orderTemplate = responseTemplateManager.createTemplate(
                botId,
                "order_status_template",
                "Template kiểm tra trạng thái đơn hàng",
                "order_inquiry",
                "📦 **Thông tin đơn hàng {{order_id}}**\\n\\n**Trạng thái:** {{status}}\\n**Ngày đặt:** {{order_date}}\\n**Dự kiến giao:** {{delivery_date}}\\n**Tổng tiền:** {{total_amount}} VNĐ\\n\\n{{delivery_message}}",
                ResponseTemplate.TemplateType.RICH_TEXT,
                "vi",
                6,
                Map.of(
                    "order_id", Map.of("type", "string", "required", true, "description", "Mã đơn hàng"),
                    "status", Map.of("type", "string", "required", true, "description", "Trạng thái đơn hàng"),
                    "order_date", Map.of("type", "date", "required", true, "description", "Ngày đặt hàng"),
                    "delivery_date", Map.of("type", "date", "required", false, "description", "Ngày giao hàng dự kiến"),
                    "total_amount", Map.of("type", "number", "required", true, "description", "Tổng số tiền"),
                    "delivery_message", Map.of("type", "string", "required", false, "description", "Thông tin giao hàng")
                ),
                java.util.List.of(
                    Map.of("title", "Chi tiết đơn hàng", "payload", "order_details"),
                    Map.of("title", "Hủy đơn hàng", "payload", "cancel_order"),
                    Map.of("title", "Liên hệ hỗ trợ", "payload", "contact_support")
                ),
                "demo-user"
            );
            log.info("✅ Created order status template: {}", orderTemplate.getName());
            
        } catch (Exception e) {
            log.error("❌ Error creating templates: {}", e.getMessage(), e);
        }
    }
    
    private void demoCustomLogicProcessing(UUID botId) {
        log.info("🧠 Demo 3: Testing Custom Logic Processing...");
        
        try {
            // Test Case 1: Greeting message
            testMessage(botId, "xin chào", "greeting", Map.of("user_name", "Anh Nam"));
            
            // Test Case 2: Order inquiry
            testMessage(botId, "kiểm tra đơn hàng DH12345", "order_inquiry", Map.of());
            
            // Test Case 3: Help keyword
            testMessage(botId, "tôi cần giúp đỡ", "general", Map.of("user_name", "Chị Lan"));
            
        } catch (Exception e) {
            log.error("❌ Error testing custom logic: {}", e.getMessage(), e);
        }
    }
    
    private void testMessage(UUID botId, String message, String intent, Map<String, Object> contextData) {
        log.info("🔍 Testing message: '{}' (intent: {})", message, intent);
        
        MiddlewareRequest request = MiddlewareRequest.builder()
            .requestId("test-" + System.currentTimeMillis())
            .userId("demo-user-001")
            .platform("demo")
            .message(message)
            .botId(botId.toString())
            .tenantKey("demo-tenant")
            .ownerId("demo-owner")
            .language("vi")
            .build();
        
        ConversationContext context = ConversationContext.builder()
            .contextId("demo-context")
            .botId(botId.toString())
            .userId("demo-user-001")
            .platform("demo")
            .language("vi")
            .metadata(contextData)
            .build();
        
        IntentAnalysisResult intentAnalysis = IntentAnalysisResult.builder()
            .primaryIntent(intent)
            .confidence(0.9)
            .language("vi")
            .build();
        
        // Process với custom logic
        MiddlewareResponse response = customLogicEngine.processWithCustomLogic(request, context, intentAnalysis);
        
        if (response != null) {
            log.info("✅ Response received:");
            log.info("   Provider: {}", response.getProviderUsed());
            log.info("   Response: {}", response.getResponse());
            log.info("   Quick Replies: {}", response.getQuickReplies() != null ? response.getQuickReplies().size() : 0);
            log.info("   Selection Reason: {}", response.getProcessingMetrics().getSelectionReason());
        } else {
            log.info("❌ No custom logic matched, would fallback to default providers");
        }
        
        log.info("---");
    }
    
    private void demoStatistics(UUID botId) {
        log.info("📊 Demo 4: Custom Logic Statistics...");
        
        try {
            CustomLogicEngine.CustomLogicStatistics stats = customLogicEngine.getCustomLogicStatistics(botId);
            
            log.info("📈 Statistics for bot {}:", botId);
            log.info("   Total Rules: {}", stats.getTotalRules());
            log.info("   Active Rules: {}", stats.getActiveRules());
            log.info("   Total Templates: {}", stats.getTotalTemplates());
            log.info("   Active Templates: {}", stats.getActiveTemplates());
            log.info("   Total Rule Executions: {}", stats.getTotalRuleExecutions());
            log.info("   Total Template Usage: {}", stats.getTotalTemplateUsage());
            log.info("   Has Custom Logic: {}", stats.isHasCustomLogic());
            
            // Test toggle functionality
            boolean toggleResult = customLogicEngine.toggleCustomLogic(botId, false);
            log.info("🔧 Toggle custom logic OFF: {}", toggleResult ? "Success" : "Failed");
            
            CustomLogicEngine.CustomLogicStatistics statsAfter = customLogicEngine.getCustomLogicStatistics(botId);
            log.info("   Active Rules after toggle: {}", statsAfter.getActiveRules());
            log.info("   Active Templates after toggle: {}", statsAfter.getActiveTemplates());
            
            // Toggle back ON
            customLogicEngine.toggleCustomLogic(botId, true);
            log.info("🔧 Toggle custom logic ON: Success");
            
        } catch (Exception e) {
            log.error("❌ Error getting statistics: {}", e.getMessage(), e);
        }
    }
}
