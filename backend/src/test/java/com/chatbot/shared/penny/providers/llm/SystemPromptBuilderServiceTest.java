package com.chatbot.shared.penny.providers.llm;

import com.chatbot.shared.penny.kb.KnowledgeChunkSearchService;
import com.chatbot.shared.penny.model.BotPersonaStyle;
import com.chatbot.shared.penny.model.LlmProviderType;
import com.chatbot.shared.penny.model.PennyBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SystemPromptBuilderService
 */
@ExtendWith(MockitoExtension.class)
class SystemPromptBuilderServiceTest {

    @Mock
    private KnowledgeChunkSearchService knowledgeChunkSearchService;

    @InjectMocks
    private SystemPromptBuilderService systemPromptBuilderService;

    private PennyBot bot;

    @BeforeEach
    void setUp() {
        bot = PennyBot.builder()
            .id(UUID.randomUUID())
            .tenantId(1L)
            .botName("Test Bot")
            .businessName("Test Business")
            .businessDescription("A test business for unit testing")
            .providerType(LlmProviderType.OPENAI)
            .modelName("gpt-4o-mini")
            .temperature(0.7f)
            .personaStyle(BotPersonaStyle.PROFESSIONAL)
            .customInstructions("Be helpful and concise")
            .greetingMessage("Hello! How can I help you?")
            .fallbackMessage("I'm sorry, I couldn't find an answer. Please contact support.")
            .build();
    }

    @Test
    @DisplayName("Should build system prompt with all sections")
    void shouldBuildSystemPromptWithAllSections() {
        when(knowledgeChunkSearchService.isEnabled()).thenReturn(true);
        when(knowledgeChunkSearchService.searchAndFormatContext(any(), any(), any())).thenReturn("RAG context content");

        String prompt = systemPromptBuilderService.buildSystemPrompt(bot, "test query");

        assertNotNull(prompt);
        assertTrue(prompt.contains("[ROLE & PERSONA]"));
        assertTrue(prompt.contains("[CUSTOM INSTRUCTIONS]"));
        assertTrue(prompt.contains("[KNOWLEDGE BASE CONTEXT (RAG)]"));
        assertTrue(prompt.contains("[RULES]"));
        assertTrue(prompt.contains("Test Business"));
        assertTrue(prompt.contains("Be helpful and concise"));
    }

    @Test
    @DisplayName("Should build system prompt without RAG when RAG is disabled")
    void shouldBuildSystemPromptWithoutRagWhenDisabled() {
        when(knowledgeChunkSearchService.isEnabled()).thenReturn(false);

        String prompt = systemPromptBuilderService.buildSystemPrompt(bot, "test query");

        assertNotNull(prompt);
        assertFalse(prompt.contains("[KNOWLEDGE BASE CONTEXT (RAG)]"));
    }

    @Test
    @DisplayName("Should build system prompt without RAG when query is null")
    void shouldBuildSystemPromptWithoutRagWhenQueryIsNull() {
        String prompt = systemPromptBuilderService.buildSystemPrompt(bot, null);

        assertNotNull(prompt);
        assertFalse(prompt.contains("[KNOWLEDGE BASE CONTEXT (RAG)]"));
    }

    @Test
    @DisplayName("Should build system prompt without RAG when query is empty")
    void shouldBuildSystemPromptWithoutRagWhenQueryIsEmpty() {
        String prompt = systemPromptBuilderService.buildSystemPrompt(bot, "");

        assertNotNull(prompt);
        assertFalse(prompt.contains("[KNOWLEDGE BASE CONTEXT (RAG)]"));
    }

    @Test
    @DisplayName("Should include business name in role section")
    void shouldIncludeBusinessName() {
        String prompt = systemPromptBuilderService.buildSystemPrompt(bot, null);

        assertTrue(prompt.contains("Test Business"));
    }

    @Test
    @DisplayName("Should include custom instructions when provided")
    void shouldIncludeCustomInstructions() {
        String prompt = systemPromptBuilderService.buildSystemPrompt(bot, null);

        assertTrue(prompt.contains("Be helpful and concise"));
    }

    @Test
    @DisplayName("Should not include custom instructions when null")
    void shouldNotIncludeCustomInstructionsWhenNull() {
        bot.setCustomInstructions(null);

        String prompt = systemPromptBuilderService.buildSystemPrompt(bot, null);

        assertFalse(prompt.contains("[CUSTOM INSTRUCTIONS]"));
    }

    @Test
    @DisplayName("Should include persona style description")
    void shouldIncludePersonaStyle() {
        String prompt = systemPromptBuilderService.buildSystemPrompt(bot, null);

        assertTrue(prompt.contains("Chuyên nghiệp"));
    }

    @Test
    @DisplayName("Should get greeting message from bot")
    void shouldGetGreetingMessageFromBot() {
        String greeting = systemPromptBuilderService.getGreetingMessage(bot);

        assertEquals("Hello! How can I help you?", greeting);
    }

    @Test
    @DisplayName("Should generate default greeting when not set")
    void shouldGenerateDefaultGreeting() {
        bot.setGreetingMessage(null);

        String greeting = systemPromptBuilderService.getGreetingMessage(bot);

        assertNotNull(greeting);
        assertTrue(greeting.contains("Test Business"));
    }

    @Test
    @DisplayName("Should generate different greetings for different personas")
    void shouldGenerateDifferentGreetingsForPersonas() {
        bot.setGreetingMessage(null);

        String professionalGreeting = systemPromptBuilderService.getGreetingMessage(bot);
        bot.setPersonaStyle(BotPersonaStyle.FRIENDLY);
        String friendlyGreeting = systemPromptBuilderService.getGreetingMessage(bot);

        // Greetings should be different based on persona
        assertNotEquals(professionalGreeting, friendlyGreeting);
        // Friendly greeting should contain friendly-related words
        assertTrue(friendlyGreeting.toLowerCase().contains("thân") || 
                   friendlyGreeting.toLowerCase().contains("vui") ||
                   friendlyGreeting.toLowerCase().contains("hỗ trợ"));
    }

    @Test
    @DisplayName("Should handle bot without business name")
    void shouldHandleBotWithoutBusinessName() {
        bot.setBusinessName(null);
        bot.setGreetingMessage(null);

        String greeting = systemPromptBuilderService.getGreetingMessage(bot);

        assertNotNull(greeting);
        assertTrue(greeting.contains("chúng tôi"));
    }

    @Test
    @DisplayName("Should include fallback message in rules")
    void shouldIncludeFallbackMessage() {
        String prompt = systemPromptBuilderService.buildSystemPrompt(bot, null);

        assertTrue(prompt.contains("I'm sorry, I couldn't find an answer"));
    }

    @Test
    @DisplayName("Should include response rules")
    void shouldIncludeResponseRules() {
        String prompt = systemPromptBuilderService.buildSystemPrompt(bot, null);

        assertTrue(prompt.contains("Ưu tiên trả lời dựa trên KNOWLEDGE BASE CONTEXT"));
        assertTrue(prompt.contains("Trả lời bằng ngôn ngữ mà khách hàng sử dụng"));
    }
}
