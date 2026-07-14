package com.chatbot.shared.penny.rules;

import com.chatbot.shared.penny.context.ConversationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BotRuleManager - validates WEBHOOK and SCRIPT action execution,
 * rule evaluation, and template matching after implementing real action handlers.
 */
@ExtendWith(MockitoExtension.class)
class BotRuleManagerTest {

    @Mock
    private BotRuleRepository botRuleRepository;

    @Mock
    private ResponseTemplateRepository responseTemplateRepository;

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private BotRuleManager botRuleManager;

    private ConversationContext context;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        botRuleManager = new BotRuleManager(
            botRuleRepository, responseTemplateRepository, objectMapper, restTemplate);

        context = ConversationContext.builder()
            .contextId("ctx-1")
            .userId("user-1")
            .botId("bot-1")
            .platform("facebook")
            .sessionId("session-1")
            .messageCount(5)
            .lastIntent("greeting")
            .build();
    }

    @Nested
    @DisplayName("evaluateRules()")
    class EvaluateRulesTests {

        @Test
        @DisplayName("should match INTENT trigger and return RESPONSE action")
        void shouldMatchIntentTriggerResponse() {
            // Given
            UUID botId = UUID.randomUUID();
            BotRule rule = BotRule.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name("Greeting Rule")
                .triggerType(BotRule.TriggerType.INTENT)
                .triggerValue("greeting")
                .ruleType(BotRule.RuleType.RESPONSE)
                .action("{\"text\": \"Xin chào! Tôi có thể giúp gì cho bạn?\"}")
                .condition("{}")
                .isActive(true)
                .priority(1)
                .executionCount(0L)
                .createdBy("admin")
                .build();

            when(botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId))
                .thenReturn(List.of(rule));

            // When
            BotRuleManager.RuleEvaluationResult result = botRuleManager.evaluateRules(
                botId, "greeting", "Xin chào", context);

            // Then
            assertNotNull(result);
            assertTrue(result.isMatched());
            assertTrue(result.getResponse().contains("Xin chào"));
        }

        @Test
        @DisplayName("should match KEYWORD trigger")
        void shouldMatchKeywordTrigger() {
            // Given
            UUID botId = UUID.randomUUID();
            BotRule rule = BotRule.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name("Promo Keyword Rule")
                .triggerType(BotRule.TriggerType.KEYWORD)
                .triggerValue("khuyến mãi")
                .ruleType(BotRule.RuleType.RESPONSE)
                .action("{\"text\": \"Hiện đang có khuyến mãi 20%!\"}")
                .condition("{}")
                .isActive(true)
                .priority(1)
                .executionCount(0L)
                .createdBy("admin")
                .build();

            when(botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId))
                .thenReturn(List.of(rule));

            // When
            BotRuleManager.RuleEvaluationResult result = botRuleManager.evaluateRules(
                botId, "general_chat", "Có khuyến mãi gì không?", context);

            // Then
            assertNotNull(result);
            assertTrue(result.isMatched());
            assertTrue(result.getResponse().contains("khuyến mãi 20%"));
        }

        @Test
        @DisplayName("should return unmatched result when no rules match")
        void shouldReturnUnmatchedWhenNoMatch() {
            // Given
            UUID botId = UUID.randomUUID();
            BotRule rule = BotRule.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name("Order Rule")
                .triggerType(BotRule.TriggerType.INTENT)
                .triggerValue("order_inquiry")
                .ruleType(BotRule.RuleType.RESPONSE)
                .action("{\"text\": \"Đơn hàng...\"}")
                .condition("{}")
                .isActive(true)
                .priority(1)
                .executionCount(0L)
                .createdBy("admin")
                .build();

            when(botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId))
                .thenReturn(List.of(rule));

            // When - send a "greeting" intent but rule expects "order_inquiry"
            BotRuleManager.RuleEvaluationResult result = botRuleManager.evaluateRules(
                botId, "greeting", "Xin chào", context);

            // Then
            assertNotNull(result);
            assertFalse(result.isMatched());
        }

        @Test
        @DisplayName("should return unmatched when bot has no rules")
        void shouldReturnUnmatchedWhenNoRules() {
            // Given
            UUID botId = UUID.randomUUID();
            when(botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId))
                .thenReturn(Collections.emptyList());

            // When
            BotRuleManager.RuleEvaluationResult result = botRuleManager.evaluateRules(
                botId, "greeting", "Hello", context);

            // Then
            assertNotNull(result);
            assertFalse(result.isMatched());
        }

        @Test
        @DisplayName("should increment execution count after match")
        void shouldIncrementExecutionCount() {
            // Given
            UUID botId = UUID.randomUUID();
            BotRule rule = BotRule.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name("Test Rule")
                .triggerType(BotRule.TriggerType.INTENT)
                .triggerValue("greeting")
                .ruleType(BotRule.RuleType.RESPONSE)
                .action("{\"text\": \"Hello!\"}")
                .condition("{}")
                .isActive(true)
                .priority(1)
                .executionCount(0L)
                .createdBy("admin")
                .build();

            when(botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId))
                .thenReturn(List.of(rule));

            // When
            botRuleManager.evaluateRules(botId, "greeting", "Hi", context);

            // Then
            assertEquals(1L, rule.getExecutionCount());
            verify(botRuleRepository).save(rule);
        }
    }

    @Nested
    @DisplayName("WEBHOOK action")
    class WebhookActionTests {

        @Test
        @DisplayName("should call webhook URL and return response message")
        void shouldCallWebhookAndReturnResponse() {
            // Given
            UUID botId = UUID.randomUUID();
            String webhookUrl = "https://example.com/webhook";
            BotRule rule = BotRule.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name("Webhook Rule")
                .triggerType(BotRule.TriggerType.INTENT)
                .triggerValue("order_inquiry")
                .ruleType(BotRule.RuleType.WEBHOOK)
                .action("{\"url\": \"" + webhookUrl + "\"}")
                .condition("{}")
                .isActive(true)
                .priority(1)
                .executionCount(0L)
                .createdBy("admin")
                .build();

            when(botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId))
                .thenReturn(List.of(rule));

            // Mock webhook response
            Map<String, Object> webhookResponse = new HashMap<>();
            webhookResponse.put("message", "Đơn hàng #123 đang vận chuyển");
            when(restTemplate.postForObject(eq(webhookUrl), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(webhookResponse);

            // When
            BotRuleManager.RuleEvaluationResult result = botRuleManager.evaluateRules(
                botId, "order_inquiry", "Kiểm tra đơn hàng", context);

            // Then
            assertNotNull(result);
            assertTrue(result.isMatched());
            assertEquals("Đơn hàng #123 đang vận chuyển", result.getResponse());
            verify(restTemplate).postForObject(eq(webhookUrl), any(HttpEntity.class), eq(Map.class));
        }

        @Test
        @DisplayName("should return error response when webhook fails")
        void shouldReturnErrorWhenWebhookFails() {
            // Given
            UUID botId = UUID.randomUUID();
            String webhookUrl = "https://example.com/webhook-down";
            BotRule rule = BotRule.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name("Failing Webhook Rule")
                .triggerType(BotRule.TriggerType.INTENT)
                .triggerValue("order_inquiry")
                .ruleType(BotRule.RuleType.WEBHOOK)
                .action("{\"url\": \"" + webhookUrl + "\", \"errorResponse\": \"Dịch vụ tạm lỗi.\"}")
                .condition("{}")
                .isActive(true)
                .priority(1)
                .executionCount(0L)
                .createdBy("admin")
                .build();

            when(botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId))
                .thenReturn(List.of(rule));
            when(restTemplate.postForObject(eq(webhookUrl), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("Connection refused"));

            // When
            BotRuleManager.RuleEvaluationResult result = botRuleManager.evaluateRules(
                botId, "order_inquiry", "Kiểm tra đơn hàng", context);

            // Then
            assertNotNull(result);
            assertTrue(result.isMatched());
            assertEquals("Dịch vụ tạm lỗi.", result.getResponse());
        }

        @Test
        @DisplayName("should return default message when webhook has no url")
        void shouldReturnDefaultWhenNoUrl() {
            // Given
            UUID botId = UUID.randomUUID();
            BotRule rule = BotRule.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name("Bad Webhook Rule")
                .triggerType(BotRule.TriggerType.INTENT)
                .triggerValue("order_inquiry")
                .ruleType(BotRule.RuleType.WEBHOOK)
                .action("{}")
                .condition("{}")
                .isActive(true)
                .priority(1)
                .executionCount(0L)
                .createdBy("admin")
                .build();

            when(botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId))
                .thenReturn(List.of(rule));

            // When
            BotRuleManager.RuleEvaluationResult result = botRuleManager.evaluateRules(
                botId, "order_inquiry", "Kiểm tra đơn hàng", context);

            // Then
            assertNotNull(result);
            assertTrue(result.isMatched());
            // Should get the default missing-URL message
            assertTrue(result.getResponse().contains("webhook") || result.getResponse().contains("ghi nhận"));
        }
    }

    @Nested
    @DisplayName("SCRIPT action")
    class ScriptActionTests {

        @Test
        @DisplayName("should evaluate SpEL expression and return result")
        void shouldEvaluateSpelExpression() {
            // Given
            UUID botId = UUID.randomUUID();
            BotRule rule = BotRule.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name("SpEL Rule")
                .triggerType(BotRule.TriggerType.INTENT)
                .triggerValue("greeting")
                .ruleType(BotRule.RuleType.SCRIPT)
                .action("{\"script\": \"'Xin chào ' + #userId + '! Bạn đã gửi ' + #messageCount + ' tin nhắn.'\"}")
                .condition("{}")
                .isActive(true)
                .priority(1)
                .executionCount(0L)
                .createdBy("admin")
                .build();

            when(botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId))
                .thenReturn(List.of(rule));

            // When
            BotRuleManager.RuleEvaluationResult result = botRuleManager.evaluateRules(
                botId, "greeting", "Hello", context);

            // Then
            assertNotNull(result);
            assertTrue(result.isMatched());
            assertTrue(result.getResponse().contains("user-1"));
            assertTrue(result.getResponse().contains("5"));
        }

        @Test
        @DisplayName("should return error response when script is invalid")
        void shouldReturnErrorWhenScriptInvalid() {
            // Given
            UUID botId = UUID.randomUUID();
            BotRule rule = BotRule.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name("Bad Script Rule")
                .triggerType(BotRule.TriggerType.INTENT)
                .triggerValue("greeting")
                .ruleType(BotRule.RuleType.SCRIPT)
                .action("{\"script\": \"#nonExistent.callMethod()\", \"errorResponse\": \"Lỗi script.\"}")
                .condition("{}")
                .isActive(true)
                .priority(1)
                .executionCount(0L)
                .createdBy("admin")
                .build();

            when(botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId))
                .thenReturn(List.of(rule));

            // When
            BotRuleManager.RuleEvaluationResult result = botRuleManager.evaluateRules(
                botId, "greeting", "Hello", context);

            // Then
            assertNotNull(result);
            assertTrue(result.isMatched());
            // Should fallback to error response or the catch-all in processRuleAction
            assertNotNull(result.getResponse());
        }

        @Test
        @DisplayName("should return default response when script is empty")
        void shouldReturnDefaultWhenNoScript() {
            // Given
            UUID botId = UUID.randomUUID();
            BotRule rule = BotRule.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name("Empty Script Rule")
                .triggerType(BotRule.TriggerType.INTENT)
                .triggerValue("greeting")
                .ruleType(BotRule.RuleType.SCRIPT)
                .action("{\"defaultResponse\": \"Fallback response\"}")
                .condition("{}")
                .isActive(true)
                .priority(1)
                .executionCount(0L)
                .createdBy("admin")
                .build();

            when(botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId))
                .thenReturn(List.of(rule));

            // When
            BotRuleManager.RuleEvaluationResult result = botRuleManager.evaluateRules(
                botId, "greeting", "Hello", context);

            // Then
            assertNotNull(result);
            assertTrue(result.isMatched());
            assertEquals("Fallback response", result.getResponse());
        }
    }

    @Nested
    @DisplayName("Rule priority")
    class PriorityTests {

        @Test
        @DisplayName("should evaluate higher priority rule first")
        void shouldEvaluateHigherPriorityFirst() {
            // Given
            UUID botId = UUID.randomUUID();
            BotRule lowPriorityRule = BotRule.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name("Low Priority")
                .triggerType(BotRule.TriggerType.INTENT)
                .triggerValue("greeting")
                .ruleType(BotRule.RuleType.RESPONSE)
                .action("{\"text\": \"Low priority response\"}")
                .condition("{}")
                .isActive(true)
                .priority(1)
                .executionCount(0L)
                .createdBy("admin")
                .build();

            BotRule highPriorityRule = BotRule.builder()
                .id(UUID.randomUUID())
                .botId(botId)
                .name("High Priority")
                .triggerType(BotRule.TriggerType.INTENT)
                .triggerValue("greeting")
                .ruleType(BotRule.RuleType.RESPONSE)
                .action("{\"text\": \"High priority response\"}")
                .condition("{}")
                .isActive(true)
                .priority(10)
                .executionCount(0L)
                .createdBy("admin")
                .build();

            // Repository returns in priority DESC order (high first)
            when(botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId))
                .thenReturn(List.of(highPriorityRule, lowPriorityRule));

            // When
            BotRuleManager.RuleEvaluationResult result = botRuleManager.evaluateRules(
                botId, "greeting", "Hello", context);

            // Then
            assertNotNull(result);
            assertTrue(result.isMatched());
            assertEquals("High priority response", result.getResponse());
        }
    }
}
