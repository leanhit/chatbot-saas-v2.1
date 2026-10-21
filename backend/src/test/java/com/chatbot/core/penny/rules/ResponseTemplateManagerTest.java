package com.chatbot.core.penny.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ResponseTemplateManager.
 * Validates CRUD, best-template selection, variable replacement,
 * inactive template guard, and usage-count increment.
 */
@ExtendWith(MockitoExtension.class)
class ResponseTemplateManagerTest {

    @Mock private ResponseTemplateRepository templateRepository;

    private ObjectMapper objectMapper;
    private ResponseTemplateManager manager;

    private UUID botId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        manager = new ResponseTemplateManager(templateRepository, objectMapper);
        botId = UUID.randomUUID();
    }

    // ── Helper builders ───────────────────────────────────────────────────────

    private ResponseTemplate buildTemplate(UUID id, String name, String intent,
                                           String lang, int priority, boolean active,
                                           String text) {
        return ResponseTemplate.builder()
            .id(id).botId(botId).name(name).intent(intent)
            .templateText(text).templateType(ResponseTemplate.TemplateType.TEXT)
            .language(lang).priority(priority).isActive(active)
            .usageCount(0L).createdBy("admin").build();
    }

    // ── createTemplate ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createTemplate()")
    class CreateTemplateTests {

        @Test
        @DisplayName("should persist template with correct fields")
        void savesWithCorrectFields() {
            when(templateRepository.existsByBotIdAndNameAndIsActiveTrue(botId, "Chào hỏi")).thenReturn(false);
            when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ResponseTemplate result = manager.createTemplate(
                botId, "Chào hỏi", "Mẫu chào", "greeting",
                "Xin chào {{user_id}}!", ResponseTemplate.TemplateType.TEXT,
                "vi", 5, null, null, "admin");

            assertNotNull(result);
            assertEquals("Chào hỏi", result.getName());
            assertEquals("greeting", result.getIntent());
            assertEquals("vi", result.getLanguage());
            assertEquals(5, result.getPriority());
            assertTrue(result.getIsActive());
            verify(templateRepository).save(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException on duplicate name")
        void duplicateName_throws() {
            when(templateRepository.existsByBotIdAndNameAndIsActiveTrue(botId, "Chào hỏi")).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () ->
                manager.createTemplate(botId, "Chào hỏi", null, "greeting",
                    "Text", ResponseTemplate.TemplateType.TEXT,
                    "vi", 0, null, null, "admin")
            );
            verify(templateRepository, never()).save(any());
        }

        @Test
        @DisplayName("should default language to 'vi' when null is passed")
        void defaultsLanguageToVi() {
            when(templateRepository.existsByBotIdAndNameAndIsActiveTrue(botId, "T")).thenReturn(false);
            when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ResponseTemplate result = manager.createTemplate(
                botId, "T", null, "greeting", "Hi",
                ResponseTemplate.TemplateType.TEXT,
                null, // language null
                0, null, null, "admin");

            assertEquals("vi", result.getLanguage());
        }

        @Test
        @DisplayName("should serialize quickReplies to JSON string")
        void serializesQuickReplies() throws Exception {
            when(templateRepository.existsByBotIdAndNameAndIsActiveTrue(botId, "QR")).thenReturn(false);
            when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            List<Map<String, Object>> qr = List.of(
                Map.of("title", "Đơn hàng", "payload", "ORDER"),
                Map.of("title", "Giá cả", "payload", "PRICE"));

            ResponseTemplate result = manager.createTemplate(
                botId, "QR", null, "menu", "Chọn mục",
                ResponseTemplate.TemplateType.TEXT, "vi", 0, null, qr, "admin");

            assertNotNull(result.getQuickReplies());
            // Should be valid JSON array
            List<?> parsed = objectMapper.readValue(result.getQuickReplies(), List.class);
            assertEquals(2, parsed.size());
        }
    }

    // ── updateTemplate ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateTemplate()")
    class UpdateTemplateTests {

        @Test
        @DisplayName("should update only provided non-null fields")
        void updatesOnlyProvidedFields() {
            UUID templateId = UUID.randomUUID();
            ResponseTemplate existing = buildTemplate(
                templateId, "Original", "greeting", "vi", 1, true, "Old text");

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(existing));
            when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ResponseTemplate result = manager.updateTemplate(
                templateId,
                "Updated Name",  // name
                null,            // description unchanged
                null,            // intent unchanged
                "New text",      // templateText
                null,            // type unchanged
                null,            // language unchanged
                10,              // priority
                null,            // isActive unchanged
                null,            // variables unchanged
                null,            // quickReplies unchanged
                "editor");

            assertEquals("Updated Name", result.getName());
            assertEquals("greeting", result.getIntent()); // unchanged
            assertEquals("New text", result.getTemplateText());
            assertEquals(10, result.getPriority());
            assertEquals("vi", result.getLanguage()); // unchanged
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when template not found")
        void templateNotFound_throws() {
            UUID templateId = UUID.randomUUID();
            when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                manager.updateTemplate(templateId, "Name", null, null, null, null,
                    null, null, null, null, null, "editor"));
        }

        @Test
        @DisplayName("should deactivate template when isActive = false")
        void deactivatesTemplate() {
            UUID templateId = UUID.randomUUID();
            ResponseTemplate existing = buildTemplate(
                templateId, "Active Template", "greeting", "vi", 1, true, "Hi");

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(existing));
            when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ResponseTemplate result = manager.updateTemplate(
                templateId, null, null, null, null, null, null, null,
                false, null, null, "admin");

            assertFalse(result.getIsActive());
        }
    }

    // ── getBestTemplate ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getBestTemplate()")
    class GetBestTemplateTests {

        @Test
        @DisplayName("should return highest priority template for intent + language")
        void returnsHighestPriorityTemplate() {
            ResponseTemplate low = buildTemplate(UUID.randomUUID(), "Low", "greeting", "vi", 1, true, "Low");
            ResponseTemplate high = buildTemplate(UUID.randomUUID(), "High", "greeting", "vi", 10, true, "High");

            // Repository returns DESC priority (high first)
            when(templateRepository.findByBotIdAndIntentAndLanguageAndIsActiveTrueOrderByPriorityDesc(
                botId, "greeting", "vi")).thenReturn(List.of(high, low));

            ResponseTemplate result = manager.getBestTemplate(botId, "greeting", "vi");

            assertNotNull(result);
            assertEquals("High", result.getName());
        }

        @Test
        @DisplayName("should fall back to 'vi' language when requested language not found")
        void fallsBackToVietnamese() {
            ResponseTemplate viTemplate = buildTemplate(UUID.randomUUID(), "VI", "greeting", "vi", 5, true, "Xin chào");

            // English query → empty; Vietnamese fallback → found
            when(templateRepository.findByBotIdAndIntentAndLanguageAndIsActiveTrueOrderByPriorityDesc(
                botId, "greeting", "en")).thenReturn(Collections.emptyList());
            when(templateRepository.findByBotIdAndIntentAndLanguageAndIsActiveTrueOrderByPriorityDesc(
                botId, "greeting", "vi")).thenReturn(List.of(viTemplate));

            ResponseTemplate result = manager.getBestTemplate(botId, "greeting", "en");

            assertNotNull(result);
            assertEquals("vi", result.getLanguage());
        }

        @Test
        @DisplayName("should return null when no templates exist for intent")
        void noTemplates_returnsNull() {
            when(templateRepository.findByBotIdAndIntentAndLanguageAndIsActiveTrueOrderByPriorityDesc(
                any(), any(), any())).thenReturn(Collections.emptyList());

            assertNull(manager.getBestTemplate(botId, "unknown_intent", "vi"));
        }
    }

    // ── processTemplate ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("processTemplate()")
    class ProcessTemplateTests {

        @Test
        @DisplayName("should replace template variables in output text")
        void replacesTemplateVariables() {
            UUID templateId = UUID.randomUUID();
            ResponseTemplate template = buildTemplate(
                templateId, "Order Status", "order_inquiry", "vi", 1, true,
                "Đơn hàng {{order_id}} đang được xử lý cho {{user_name}}.");

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
            when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Map<String, Object> vars = Map.of(
                "order_id", "#ABC123",
                "user_name", "Nguyễn Văn A");

            ResponseTemplateManager.TemplateProcessResult result =
                manager.processTemplate(templateId, vars);

            assertNotNull(result);
            assertTrue(result.getResponse().contains("#ABC123"));
            assertTrue(result.getResponse().contains("Nguyễn Văn A"));
        }

        @Test
        @DisplayName("should increment usageCount after processing")
        void incrementsUsageCount() {
            UUID templateId = UUID.randomUUID();
            ResponseTemplate template = buildTemplate(
                templateId, "Usage Count Test", "greeting", "vi", 1, true, "Xin chào!");

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
            when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            manager.processTemplate(templateId, Collections.emptyMap());

            assertEquals(1L, template.getUsageCount());
            verify(templateRepository).save(template);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for inactive template")
        void inactiveTemplate_throws() {
            UUID templateId = UUID.randomUUID();
            ResponseTemplate inactive = buildTemplate(
                templateId, "Inactive", "greeting", "vi", 1, false, "disabled");

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(inactive));

            assertThrows(IllegalArgumentException.class, () ->
                manager.processTemplate(templateId, Collections.emptyMap()));
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when template not found")
        void notFound_throws() {
            UUID templateId = UUID.randomUUID();
            when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                manager.processTemplate(templateId, Collections.emptyMap()));
        }

        @Test
        @DisplayName("should parse quickReplies JSON and include in result")
        void parsesQuickReplies() throws Exception {
            UUID templateId = UUID.randomUUID();
            ResponseTemplate template = buildTemplate(
                templateId, "QR Template", "menu", "vi", 1, true, "Chọn:");
            template.setQuickReplies("[{\"title\":\"Giá\",\"payload\":\"PRICE\"}]");

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
            when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ResponseTemplateManager.TemplateProcessResult result =
                manager.processTemplate(templateId, Collections.emptyMap());

            assertNotNull(result.getQuickReplies());
            assertEquals(1, result.getQuickReplies().size());
        }
    }

    // ── deleteTemplate ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteTemplate()")
    class DeleteTemplateTests {

        @Test
        @DisplayName("should return true on successful soft delete")
        void successfulDelete_returnsTrue() {
            UUID templateId = UUID.randomUUID();
            doNothing().when(templateRepository).softDeleteTemplate(templateId);

            assertTrue(manager.deleteTemplate(templateId, "admin"));
            verify(templateRepository).softDeleteTemplate(templateId);
        }

        @Test
        @DisplayName("should return false when repository throws")
        void repositoryThrows_returnsFalse() {
            UUID templateId = UUID.randomUUID();
            doThrow(new RuntimeException("DB error"))
                .when(templateRepository).softDeleteTemplate(templateId);

            assertFalse(manager.deleteTemplate(templateId, "admin"));
        }
    }

    // ── getTemplateStatistics ─────────────────────────────────────────────────

    @Nested
    @DisplayName("getTemplateStatistics()")
    class StatisticsTests {

        @Test
        @DisplayName("should return correct total usage and usage-by-intent breakdown")
        void returnsCorrectStats() {
            ResponseTemplate t1 = buildTemplate(UUID.randomUUID(), "T1", "greeting", "vi", 1, true, "Hi");
            t1.setUsageCount(10L);
            ResponseTemplate t2 = buildTemplate(UUID.randomUUID(), "T2", "order_inquiry", "vi", 1, true, "Order");
            t2.setUsageCount(5L);
            ResponseTemplate t3 = buildTemplate(UUID.randomUUID(), "T3", "greeting", "vi", 2, true, "Chào");
            t3.setUsageCount(3L);

            when(templateRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId))
                .thenReturn(List.of(t1, t2, t3));
            when(templateRepository.findMostUsedTemplates(botId))
                .thenReturn(List.of(t1, t2));

            ResponseTemplateManager.TemplateStatistics stats = manager.getTemplateStatistics(botId);

            assertEquals(3, stats.getTotalTemplates());
            assertEquals(18L, stats.getTotalUsage()); // 10 + 5 + 3
            assertEquals(13L, stats.getUsageByIntent().get("greeting"));  // 10 + 3
            assertEquals(5L, stats.getUsageByIntent().get("order_inquiry"));
            assertEquals(2, stats.getMostUsedTemplates().size());
        }
    }
}
