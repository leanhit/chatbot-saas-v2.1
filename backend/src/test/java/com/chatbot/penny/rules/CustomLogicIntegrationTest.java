package com.chatbot.penny.rules;

import com.chatbot.penny.context.ConversationContext;
import com.chatbot.penny.dto.request.MiddlewareRequest;
import com.chatbot.penny.dto.response.MiddlewareResponse;
import com.chatbot.penny.routing.dto.IntentAnalysisResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test cho Custom Logic Engine
 */
@SpringBootTest
@ActiveProfiles("test")
public class CustomLogicIntegrationTest {

    @Autowired
    private CustomLogicEngine customLogicEngine;

    @Autowired
    private BotRuleManager botRuleManager;

    @Autowired
    private ResponseTemplateManager responseTemplateManager;

    @Test
    public void testCustomLogicEngine() {
        // Tạo test bot ID
        UUID botId = UUID.randomUUID();
        
        // Tạo một rule test
        BotRule rule = botRuleManager.createRule(
            botId,
            "test_greeting_rule",
            "Rule test cho greeting",
            "intent == 'greeting'",
            "{\"text\": \"Xin chào {{user_name}}! Đây là phản hồi từ custom rule.\"}",
            BotRule.RuleType.RESPONSE,
            BotRule.TriggerType.INTENT,
            "greeting",
            1,
            "test-user"
        );
        
        assertNotNull(rule);
        assertEquals("test_greeting_rule", rule.getName());
        assertTrue(rule.getIsActive());
        
        // Tạo một template test
        ResponseTemplate template = responseTemplateManager.createTemplate(
            botId,
            "test_greeting_template",
            "Template test cho greeting",
            "greeting",
            "Chào {{user_name}}, tôi là bot {{bot_name}}. Rất vui được phục vụ bạn!",
            ResponseTemplate.TemplateType.TEXT,
            "vi",
            2,
            Map.of("user_name", Map.of("type", "string", "required", true)),
            null,
            "test-user"
        );
        
        assertNotNull(template);
        assertEquals("test_greeting_template", template.getName());
        assertTrue(template.getIsActive());
        
        // Test custom logic engine
        MiddlewareRequest request = MiddlewareRequest.builder()
            .requestId("test-123")
            .userId("test-user-001")
            .platform("test")
            .message("xin chào")
            .botId(botId.toString())
            .tenantId(1L)
            .ownerId("test-owner")
            .language("vi")
            .build();
        
        ConversationContext context = ConversationContext.builder()
            .contextId("test-context")
            .botId(botId.toString())
            .userId("test-user-001")
            .platform("test")
            .language("vi")
            .metadata(Map.of("user_name", "Nguyễn Văn A"))
            .build();
        
        IntentAnalysisResult intent = IntentAnalysisResult.builder()
            .primaryIntent("greeting")
            .confidence(0.9)
            .language("vi")
            .build();
        
        // Process với custom logic
        MiddlewareResponse response = customLogicEngine.processWithCustomLogic(request, context, intent);
        
        assertNotNull(response);
        assertEquals("test-123", response.getRequestId());
        assertTrue(response.isSuccess());
        assertNotNull(response.getResponse());
        
        // Verify rule được ưu tiên hơn template (priority cao hơn)
        assertTrue(response.getResponse().contains("custom rule"));
        
        System.out.println("✅ Custom Logic Test Results:");
        System.out.println("Rule ID: " + rule.getId());
        System.out.println("Template ID: " + template.getId());
        System.out.println("Response: " + response.getResponse());
        System.out.println("Provider Used: " + response.getProviderUsed());
        System.out.println("Selection Reason: " + response.getProcessingMetrics().getSelectionReason());
        
        // Test statistics
        CustomLogicEngine.CustomLogicStatistics stats = customLogicEngine.getCustomLogicStatistics(botId);
        assertTrue(stats.isHasCustomLogic());
        assertEquals(1, stats.getTotalRules());
        assertEquals(1, stats.getTotalTemplates());
        assertEquals(1, stats.getActiveRules());
        assertEquals(1, stats.getActiveTemplates());
        
        System.out.println("✅ Statistics:");
        System.out.println("Total Rules: " + stats.getTotalRules());
        System.out.println("Total Templates: " + stats.getTotalTemplates());
        System.out.println("Has Custom Logic: " + stats.isHasCustomLogic());
        
        // Cleanup
        botRuleManager.deleteRule(rule.getId(), "test-user");
        responseTemplateManager.deleteTemplate(template.getId(), "test-user");
        
        System.out.println("✅ Test completed successfully!");
    }
    
    @Test
    public void testTemplateProcessing() {
        UUID botId = UUID.randomUUID();
        
        // Tạo template với variables
        ResponseTemplate template = responseTemplateManager.createTemplate(
            botId,
            "order_status_template",
            "Template cho kiểm tra đơn hàng",
            "order_inquiry",
            "Đơn hàng {{order_id}} của bạn đang ở trạng thái: {{status}}. Dự kiến giao: {{delivery_date}}",
            ResponseTemplate.TemplateType.TEXT,
            "vi",
            1,
            null,
            null,
            "test-user"
        );
        
        // Process template với variables
        Map<String, Object> variables = Map.of(
            "order_id", "DH12345",
            "status", "Đang giao hàng",
            "delivery_date", "25/01/2026"
        );
        
        ResponseTemplateManager.TemplateProcessResult result = 
            responseTemplateManager.processTemplate(template.getId(), variables);
        
        assertNotNull(result);
        assertTrue(result.getResponse().contains("DH12345"));
        assertTrue(result.getResponse().contains("Đang giao hàng"));
        assertTrue(result.getResponse().contains("25/01/2026"));
        
        System.out.println("✅ Template Processing Test:");
        System.out.println("Template: " + template.getName());
        System.out.println("Processed Response: " + result.getResponse());
        
        // Cleanup
        responseTemplateManager.deleteTemplate(template.getId(), "test-user");
        
        System.out.println("✅ Template test completed successfully!");
    }
}
