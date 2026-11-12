package com.chatbot.core.penny.providers.llm;

import com.chatbot.core.penny.model.LlmProviderType;
import com.chatbot.core.penny.model.PennyBot;
import com.chatbot.core.penny.model.PennyBotType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for LlmRoutingService: per-request model isolation and fallback order.
 */
@ExtendWith(MockitoExtension.class)
class LlmRoutingServiceTest {

    @Mock private OpenAiLlmProvider openAiProvider;
    @Mock private ClaudeLlmProvider claudeProvider;
    @Mock private GeminiLlmProvider geminiProvider;
    @Mock private OllamaLlmProvider ollamaProvider;
    @Mock private CircuitBreakerRegistry circuitBreakerRegistry;
    @Mock private io.micrometer.core.instrument.MeterRegistry meterRegistry;
    @Mock private org.springframework.beans.factory.ObjectProvider<io.micrometer.core.instrument.MeterRegistry> meterRegistryProvider;

    private LlmRoutingService routingService;

    @BeforeEach
    void setUp() {
        lenient().when(meterRegistryProvider.getIfAvailable()).thenReturn(meterRegistry);
        routingService = new LlmRoutingService(
            openAiProvider, claudeProvider, geminiProvider, ollamaProvider,
            circuitBreakerRegistry, meterRegistryProvider);
        // Real circuit breakers pass through to the supplier
        lenient().when(circuitBreakerRegistry.circuitBreaker(anyString()))
            .thenAnswer(inv -> io.github.resilience4j.circuitbreaker.CircuitBreaker.ofDefaults(inv.getArgument(0)));
    }

    private PennyBot bot(LlmProviderType primary, LlmProviderType fallback, String model) {
        return PennyBot.builder()
            .id(UUID.randomUUID()).tenantId(1L).ownerId("o")
            .botName("b").botType(PennyBotType.SUPPORT)
            .pennyBotId("p").isActive(true).isEnabled(true)
            .providerType(primary).fallbackProviderType(fallback)
            .modelName(model).build();
    }

    private LlmRequest request() {
        return LlmRequest.builder().userMessage("hi").build();
    }

    @Test
    @DisplayName("primary success -> provider used, model passed per-request (no shared state)")
    void primarySuccess() {
        PennyBot b = bot(LlmProviderType.OPENAI, null, "gpt-4o");
        when(openAiProvider.isHealthy()).thenReturn(true);
        when(openAiProvider.generateResponse(any()))
            .thenReturn(LlmResponse.builder().success(true).text("ok").build());

        LlmResponse resp = routingService.generateResponse(request(), b);

        assertTrue(resp.isSuccess());
        verify(openAiProvider).generateResponse(argThat(r -> "gpt-4o".equals(r.getModelName())));
        verifyNoInteractions(claudeProvider, geminiProvider, ollamaProvider);
    }

    @Test
    @DisplayName("primary fails -> bot-configured fallback tried first")
    void configuredFallbackFirst() {
        PennyBot b = bot(LlmProviderType.OPENAI, LlmProviderType.GEMINI, "gpt-4o");
        when(openAiProvider.isHealthy()).thenReturn(true);
        when(openAiProvider.generateResponse(any()))
            .thenReturn(LlmResponse.failure("down"));
        when(geminiProvider.isHealthy()).thenReturn(true);
        when(geminiProvider.generateResponse(any()))
            .thenReturn(LlmResponse.builder().success(true).text("gemini-ok").build());

        LlmResponse resp = routingService.generateResponse(request(), b);

        assertTrue(resp.isSuccess());
        assertEquals("gemini-ok", resp.getText());
        // model override cleared for fallback -> provider default used
        verify(geminiProvider).generateResponse(argThat(r -> r.getModelName() == null));
        verifyNoInteractions(claudeProvider, ollamaProvider);
    }

    @Test
    @DisplayName("primary fails, no bot fallback -> default order OpenAI->Claude->Gemini->Ollama")
    void defaultFallbackOrder() {
        PennyBot b = bot(LlmProviderType.OPENAI, null, "gpt-4o");
        when(openAiProvider.isHealthy()).thenReturn(true);
        when(openAiProvider.generateResponse(any())).thenReturn(LlmResponse.failure("down"));
        when(claudeProvider.isHealthy()).thenReturn(true);
        when(claudeProvider.generateResponse(any()))
            .thenReturn(LlmResponse.builder().success(true).text("claude-ok").build());

        LlmResponse resp = routingService.generateResponse(request(), b);

        assertTrue(resp.isSuccess());
        assertEquals("claude-ok", resp.getText());
        verifyNoInteractions(geminiProvider, ollamaProvider);
    }

    @Test
    @DisplayName("all providers fail -> failure response")
    void allFail() {
        PennyBot b = bot(LlmProviderType.OPENAI, null, "gpt-4o");
        when(openAiProvider.isHealthy()).thenReturn(true);
        when(openAiProvider.generateResponse(any())).thenReturn(LlmResponse.failure("down"));
        when(claudeProvider.isHealthy()).thenReturn(false);
        when(geminiProvider.isHealthy()).thenReturn(false);
        when(ollamaProvider.isHealthy()).thenReturn(false);

        LlmResponse resp = routingService.generateResponse(request(), b);

        assertFalse(resp.isSuccess());
    }

    @Test
    @DisplayName("unhealthy primary skipped to fallback")
    void unhealthyPrimary() {
        PennyBot b = bot(LlmProviderType.OPENAI, null, "gpt-4o");
        when(openAiProvider.isHealthy()).thenReturn(false);
        when(claudeProvider.isHealthy()).thenReturn(true);
        when(claudeProvider.generateResponse(any()))
            .thenReturn(LlmResponse.builder().success(true).text("ok").build());

        LlmResponse resp = routingService.generateResponse(request(), b);

        assertTrue(resp.isSuccess());
        verify(openAiProvider, never()).generateResponse(any());
    }
}
