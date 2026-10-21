package com.chatbot.core.penny.rules;

import com.chatbot.core.penny.context.ConversationContext;
import com.chatbot.core.penny.dto.request.MiddlewareRequest;
import com.chatbot.core.penny.dto.response.MiddlewareResponse;
import com.chatbot.core.penny.routing.dto.IntentAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomLogicEngine.
 * Validates rule-matching, template-fallback, null-handling,
 * statistics, and toggle logic.
 */
@ExtendWith(MockitoExtension.class)
class CustomLogicEngineTest {

    @Mock private BotRuleManager botRuleManager;
    @Mock private ResponseTemplateManager responseTemplateManager;

    private CustomLogicEngine engine;

    private MiddlewareRequest request;
    private ConversationContext context;
    private IntentAnalysisResult intentAnalysis;
    private UUID botId;

    @BeforeEach
    void setUp() {
        engine = new CustomLogicEngine(botRuleManager, responseTemplateManager);

        botId = UUID.randomUUID();

        request = MiddlewareRequest.builder()
            .requestId("req-1")
            .userId("user-fb-123")
            .botId(botId.toString())
            .platform("facebook")
            .message("Xin chào")
            .language("vi")
            .timestamp(Instant.now())
            .build();

        context = ConversationContext.builder()
            .contextId("ctx-1")
            .userId("user-fb-123")
            .botId(botId.toString())
            .platform("facebook")
            .sessionId("session-1")
            .messageCount(3)
            .language("vi")
            .build();

        intentAnalysis = IntentAnalysisResult.builder()
            .primaryIntent("greeting")
            .confidence(0.95)
            .complexity("low")
            .build();
    }

    // ── Helper builders ──────────────────────────────────────────────────────

    private BotRuleManager.RuleEvaluationResult matchedRule(String ruleName, String response) {
        BotRule rule = BotRule.builder()
            .id(UUID.randomUUID()).botId(botId).name(ruleName)
            .triggerType(BotRule.TriggerType.INTENT).triggerValue("greeting")
            .ruleType(BotRule.RuleType.RESPONSE)
            .action("{\"text\": \"" + response + "\"}")
            .condition("{}").isActive(true).priority(1).executionCount(1L).createdBy("admin")
            .build();
        return BotRuleManager.RuleEvaluationResult.builder()
            .matched(true).rule(rule).response(response)
            .ruleType(BotRule.RuleType.RESPONSE).build();
    }

    private BotRuleManager.RuleEvaluationResult noMatch() {
        return BotRuleManager.RuleEvaluationResult.builder().matched(false).build();
    }

    // ── processWithCustomLogic ────────────────────────────────────────────────

    @Nested
    @DisplayName("processWithCustomLogic()")
    class ProcessWithCustomLogicTests {

        @Test
        @DisplayName("should return CUSTOM_RULE response when a rule matches")
        void ruleMatched_returnsCustomRuleResponse() {
            when(botRuleManager.evaluateRules(eq(botId), eq("greeting"), anyString(), any()))
                .thenReturn(matchedRule("Chào hỏi", "Xin chào! Tôi có thể giúp gì?"));

            MiddlewareResponse response = engine.processWithCustomLogic(request, context, intentAnalysis);

            assertNotNull(response);
            assertEquals("success", response.getStatus());
            assertEquals("CUSTOM_RULE", response.getProviderUsed());
            assertEquals("Xin chào! Tôi có thể giúp gì?", response.getResponse());
            assertTrue(response.isShouldSendResponse());
            // template should NOT be consulted
            verify(responseTemplateManager, never()).getBestTemplate(any(), any(), any());
        }

        @Test
        @DisplayName("should fall back to template when rule does not match")
        void noRuleMatch_templateMatched_returnsTemplateResponse() {
            when(botRuleManager.evaluateRules(any(), any(), any(), any()))
                .thenReturn(noMatch());

            ResponseTemplate template = ResponseTemplate.builder()
                .id(UUID.randomUUID()).botId(botId)
                .name("Greeting Template").intent("greeting")
                .templateText("Chào bạn! Tôi là bot hỗ trợ.")
                .templateType(ResponseTemplate.TemplateType.TEXT)
                .language("vi").priority(1).isActive(true).usageCount(0L)
                .build();

            ResponseTemplateManager.TemplateProcessResult templateResult =
                ResponseTemplateManager.TemplateProcessResult.builder()
                    .templateId(template.getId())
                    .response("Chào bạn! Tôi là bot hỗ trợ.")
                    .templateType(ResponseTemplate.TemplateType.TEXT)
                    .language("vi")
                    .build();

            when(responseTemplateManager.getBestTemplate(eq(botId), eq("greeting"), eq("vi")))
                .thenReturn(template);
            when(responseTemplateManager.processTemplate(eq(template.getId()), any()))
                .thenReturn(templateResult);

            MiddlewareResponse response = engine.processWithCustomLogic(request, context, intentAnalysis);

            assertNotNull(response);
            assertEquals("success", response.getStatus());
            assertEquals("CUSTOM_TEMPLATE", response.getProviderUsed());
            assertEquals("Chào bạn! Tôi là bot hỗ trợ.", response.getResponse());
            assertTrue(response.isShouldSendResponse());
        }

        @Test
        @DisplayName("should return null when neither rule nor template matches")
        void noRuleNoTemplate_returnsNull() {
            when(botRuleManager.evaluateRules(any(), any(), any(), any()))
                .thenReturn(noMatch());
            when(responseTemplateManager.getBestTemplate(any(), any(), any()))
                .thenReturn(null);

            MiddlewareResponse response = engine.processWithCustomLogic(request, context, intentAnalysis);

            assertNull(response); // Let fallback provider handle
        }

        @Test
        @DisplayName("should return null when botId is not valid UUID")
        void invalidBotId_returnsNull() {
            MiddlewareRequest badRequest = MiddlewareRequest.builder()
                .requestId("req-bad").userId("user-1")
                .botId("NOT-A-UUID")           // ← invalid UUID
                .platform("facebook").message("hello")
                .timestamp(Instant.now()).build();

            MiddlewareResponse response = engine.processWithCustomLogic(
                badRequest, context, intentAnalysis);

            // Exception caught internally → returns null
            assertNull(response);
        }

        @Test
        @DisplayName("should return null and not propagate exception when rule manager throws")
        void ruleManagerThrows_returnsNull() {
            when(botRuleManager.evaluateRules(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("DB connection failed"));

            MiddlewareResponse response = engine.processWithCustomLogic(request, context, intentAnalysis);

            assertNull(response); // Graceful: no exception propagated
        }

        @Test
        @DisplayName("should return null and not propagate exception when template processing throws")
        void templateProcessingThrows_returnsNull() {
            when(botRuleManager.evaluateRules(any(), any(), any(), any()))
                .thenReturn(noMatch());

            ResponseTemplate template = ResponseTemplate.builder()
                .id(UUID.randomUUID()).botId(botId).name("Bad Template")
                .intent("greeting").templateText("{{broken}}")
                .templateType(ResponseTemplate.TemplateType.TEXT)
                .language("vi").priority(1).isActive(true).usageCount(0L)
                .build();

            when(responseTemplateManager.getBestTemplate(any(), any(), any()))
                .thenReturn(template);
            when(responseTemplateManager.processTemplate(any(), any()))
                .thenThrow(new RuntimeException("Template processing failed"));

            MiddlewareResponse response = engine.processWithCustomLogic(request, context, intentAnalysis);

            assertNull(response);
        }

        @Test
        @DisplayName("should use 'vi' as default language when context.language is null")
        void nullContextLanguage_fallsBackToVi() {
            ConversationContext noLangCtx = ConversationContext.builder()
                .contextId("ctx-nolang").userId("user-1").botId(botId.toString())
                .platform("facebook").messageCount(1)
                // language = null
                .build();

            when(botRuleManager.evaluateRules(any(), any(), any(), any()))
                .thenReturn(noMatch());
            when(responseTemplateManager.getBestTemplate(eq(botId), eq("greeting"), eq("vi")))
                .thenReturn(null);

            engine.processWithCustomLogic(request, noLangCtx, intentAnalysis);

            // Should have queried with "vi" not null
            verify(responseTemplateManager).getBestTemplate(eq(botId), eq("greeting"), eq("vi"));
        }

        @Test
        @DisplayName("CUSTOM_RULE response should include intent analysis in metadata")
        void ruleMatched_includesIntentAnalysis() {
            when(botRuleManager.evaluateRules(any(), any(), any(), any()))
                .thenReturn(matchedRule("Rule A", "OK"));

            MiddlewareResponse response = engine.processWithCustomLogic(request, context, intentAnalysis);

            assertNotNull(response);
            assertNotNull(response.getIntentAnalysis());
            assertEquals("greeting", response.getIntentAnalysis().getPrimaryIntent());
        }
    }

    // ── hasCustomLogic ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("hasCustomLogic()")
    class HasCustomLogicTests {

        @Test
        @DisplayName("should return true when bot has at least one rule")
        void withRules_returnsTrue() {
            BotRule rule = BotRule.builder().id(UUID.randomUUID()).botId(botId)
                .name("R").triggerType(BotRule.TriggerType.INTENT).triggerValue("x")
                .ruleType(BotRule.RuleType.RESPONSE).action("{}").condition("{}")
                .isActive(true).priority(0).executionCount(0L).createdBy("a").build();

            when(botRuleManager.getRulesForBot(botId)).thenReturn(List.of(rule));
            when(responseTemplateManager.getTemplatesForBot(botId)).thenReturn(Collections.emptyList());

            assertTrue(engine.hasCustomLogic(botId));
        }

        @Test
        @DisplayName("should return true when bot has at least one template")
        void withTemplates_returnsTrue() {
            ResponseTemplate t = ResponseTemplate.builder().id(UUID.randomUUID()).botId(botId)
                .name("T").intent("g").templateText("Hi").language("vi").priority(0)
                .isActive(true).usageCount(0L).templateType(ResponseTemplate.TemplateType.TEXT)
                .build();

            when(botRuleManager.getRulesForBot(botId)).thenReturn(Collections.emptyList());
            when(responseTemplateManager.getTemplatesForBot(botId)).thenReturn(List.of(t));

            assertTrue(engine.hasCustomLogic(botId));
        }

        @Test
        @DisplayName("should return false when bot has no rules and no templates")
        void withNothing_returnsFalse() {
            when(botRuleManager.getRulesForBot(botId)).thenReturn(Collections.emptyList());
            when(responseTemplateManager.getTemplatesForBot(botId)).thenReturn(Collections.emptyList());

            assertFalse(engine.hasCustomLogic(botId));
        }
    }

    // ── getCustomLogicStatistics ──────────────────────────────────────────────

    @Nested
    @DisplayName("getCustomLogicStatistics()")
    class StatisticsTests {

        @Test
        @DisplayName("should return correct counts for rules and templates")
        void returnsCorrectCounts() {
            BotRule activeRule = BotRule.builder().id(UUID.randomUUID()).botId(botId)
                .name("A").triggerType(BotRule.TriggerType.INTENT).triggerValue("x")
                .ruleType(BotRule.RuleType.RESPONSE).action("{}").condition("{}")
                .isActive(true).priority(1).executionCount(10L).createdBy("a").build();
            BotRule inactiveRule = BotRule.builder().id(UUID.randomUUID()).botId(botId)
                .name("B").triggerType(BotRule.TriggerType.INTENT).triggerValue("y")
                .ruleType(BotRule.RuleType.RESPONSE).action("{}").condition("{}")
                .isActive(false).priority(0).executionCount(5L).createdBy("a").build();

            ResponseTemplate activeTemplate = ResponseTemplate.builder()
                .id(UUID.randomUUID()).botId(botId).name("T1").intent("g")
                .templateText("Hi").language("vi").priority(1).isActive(true)
                .usageCount(7L).templateType(ResponseTemplate.TemplateType.TEXT).build();

            when(botRuleManager.getRulesForBot(botId)).thenReturn(List.of(activeRule, inactiveRule));
            when(responseTemplateManager.getTemplatesForBot(botId)).thenReturn(List.of(activeTemplate));

            CustomLogicEngine.CustomLogicStatistics stats = engine.getCustomLogicStatistics(botId);

            assertEquals(2, stats.getTotalRules());
            assertEquals(1, stats.getActiveRules());   // only 1 active
            assertEquals(15L, stats.getTotalRuleExecutions()); // 10 + 5
            assertEquals(1, stats.getTotalTemplates());
            assertEquals(1, stats.getActiveTemplates());
            assertEquals(7L, stats.getTotalTemplateUsage());
            assertTrue(stats.isHasCustomLogic());
        }

        @Test
        @DisplayName("should return zeros when bot has no logic")
        void emptyBot_returnsAllZeros() {
            when(botRuleManager.getRulesForBot(botId)).thenReturn(Collections.emptyList());
            when(responseTemplateManager.getTemplatesForBot(botId)).thenReturn(Collections.emptyList());

            CustomLogicEngine.CustomLogicStatistics stats = engine.getCustomLogicStatistics(botId);

            assertEquals(0, stats.getTotalRules());
            assertEquals(0, stats.getTotalTemplates());
            assertEquals(0, stats.getTotalRuleExecutions());
            assertFalse(stats.isHasCustomLogic());
        }
    }

    // ── toggleCustomLogic ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("toggleCustomLogic()")
    class ToggleTests {

        @Test
        @DisplayName("should disable all rules and templates when toggled off")
        void disablesAllLogic() {
            BotRule rule = BotRule.builder().id(UUID.randomUUID()).botId(botId)
                .name("R").triggerType(BotRule.TriggerType.INTENT).triggerValue("x")
                .ruleType(BotRule.RuleType.RESPONSE).action("{}").condition("{}")
                .isActive(true).priority(1).executionCount(0L).createdBy("a").build();
            ResponseTemplate t = ResponseTemplate.builder()
                .id(UUID.randomUUID()).botId(botId).name("T").intent("g")
                .templateText("Hi").language("vi").priority(0).isActive(true)
                .usageCount(0L).templateType(ResponseTemplate.TemplateType.TEXT).build();

            when(botRuleManager.getRulesForBot(botId)).thenReturn(List.of(rule));
            when(responseTemplateManager.getTemplatesForBot(botId)).thenReturn(List.of(t));
            when(botRuleManager.updateRule(any(), any(), any(), any(), any(), any(), eq(false), any()))
                .thenReturn(rule);
            when(responseTemplateManager.updateTemplate(any(), any(), any(), any(), any(), any(), any(),
                anyInt(), eq(false), any(), any(), any()))
                .thenReturn(t);

            boolean result = engine.toggleCustomLogic(botId, false);

            assertTrue(result);
            verify(botRuleManager).updateRule(eq(rule.getId()), any(), any(), any(), any(), any(), eq(false), eq("system"));
            verify(responseTemplateManager).updateTemplate(eq(t.getId()), any(), any(), any(), any(), any(), any(),
                anyInt(), eq(false), any(), any(), eq("system"));
        }

        @Test
        @DisplayName("should return false when toggle throws exception")
        void throwsException_returnsFalse() {
            when(botRuleManager.getRulesForBot(botId))
                .thenThrow(new RuntimeException("DB down"));

            boolean result = engine.toggleCustomLogic(botId, true);

            assertFalse(result);
        }
    }
}
