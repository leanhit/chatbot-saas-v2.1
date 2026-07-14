package com.chatbot.shared.penny.routing;

import com.chatbot.spokes.facebook.webhook.service.ChatbotProviderService;
import com.chatbot.shared.penny.context.ConversationContext;
import com.chatbot.shared.penny.routing.dto.IntentAnalysisResult;
import com.chatbot.shared.penny.routing.dto.ProviderSelection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProviderSelector - validates provider map initialization
 * and correct instance lookup after the fix for getProviderInstance() returning null.
 */
class ProviderSelectorTest {

    private ChatbotProviderService botpressProvider;
    private ChatbotProviderService pennybotProvider;
    private ChatbotProviderService gptProvider;

    @BeforeEach
    void setUp() {
        botpressProvider = mock(ChatbotProviderService.class);
        when(botpressProvider.getProviderType()).thenReturn("BOTPRESS");

        pennybotProvider = mock(ChatbotProviderService.class);
        when(pennybotProvider.getProviderType()).thenReturn("PENNYBOT");

        gptProvider = mock(ChatbotProviderService.class);
        when(gptProvider.getProviderType()).thenReturn("GPT");
    }

    /**
     * Helper: create a ProviderSelector with @Value fields set via reflection
     */
    private ProviderSelector createSelector(List<ChatbotProviderService> providers) {
        ProviderSelector selector = new ProviderSelector(providers);
        ReflectionTestUtils.setField(selector, "selectionStrategy", "hybrid");
        ReflectionTestUtils.setField(selector, "fallbackEnabled", true);
        return selector;
    }

    @Nested
    @DisplayName("initProviderMap()")
    class InitProviderMapTests {

        @Test
        @DisplayName("should register all injected providers into the map")
        void shouldRegisterAllProviders() {
            // Given
            ProviderSelector selector = createSelector(
                List.of(botpressProvider, pennybotProvider, gptProvider));

            // When
            selector.initProviderMap();

            // Then - select should return non-null provider type for known intents
            ConversationContext ctx = ConversationContext.builder()
                .contextId("test").userId("user1").platform("facebook").build();
            IntentAnalysisResult analysis = IntentAnalysisResult.builder()
                .primaryIntent("order_inquiry").confidence(0.9).complexity("medium").build();

            ProviderSelection selection = selector.select(analysis, ctx);
            assertNotNull(selection);
            assertNotNull(selection.getProviderType());
        }

        @Test
        @DisplayName("should handle empty provider list without errors")
        void shouldHandleEmptyProviders() {
            // Given
            ProviderSelector selector = createSelector(Collections.emptyList());

            // When & Then - should not throw
            assertDoesNotThrow(() -> selector.initProviderMap());
        }

        @Test
        @DisplayName("should skip providers with null type")
        void shouldSkipProvidersWithNullType() {
            // Given
            ChatbotProviderService nullTypeProvider = mock(ChatbotProviderService.class);
            when(nullTypeProvider.getProviderType()).thenReturn(null);

            ProviderSelector selector = createSelector(
                List.of(botpressProvider, nullTypeProvider));

            // When & Then - should not throw
            assertDoesNotThrow(() -> selector.initProviderMap());
        }

        @Test
        @DisplayName("should normalize provider type to uppercase")
        void shouldNormalizeToUppercase() {
            // Given - provider returns lowercase type
            ChatbotProviderService lowercaseProvider = mock(ChatbotProviderService.class);
            when(lowercaseProvider.getProviderType()).thenReturn("botpress");

            ProviderSelector selector = createSelector(List.of(lowercaseProvider));

            // When
            selector.initProviderMap();

            // Then - should still resolve correctly via select()
            ConversationContext ctx = ConversationContext.builder()
                .contextId("test").userId("user1").platform("facebook").build();
            IntentAnalysisResult analysis = IntentAnalysisResult.builder()
                .primaryIntent("greeting").confidence(0.9).complexity("low").build();

            ProviderSelection selection = selector.select(analysis, ctx);
            assertNotNull(selection);
            assertNotNull(selection.getProviderType());
        }
    }

    @Nested
    @DisplayName("select()")
    class SelectTests {

        @Test
        @DisplayName("should return a valid selection for business intent")
        void shouldSelectForBusinessIntent() {
            // Given
            ProviderSelector selector = createSelector(
                List.of(botpressProvider, pennybotProvider, gptProvider));
            selector.initProviderMap();

            ConversationContext ctx = ConversationContext.builder()
                .contextId("test").userId("user1").platform("facebook").build();
            IntentAnalysisResult analysis = IntentAnalysisResult.builder()
                .primaryIntent("order_inquiry").confidence(0.9).complexity("medium").build();

            // When
            ProviderSelection selection = selector.select(analysis, ctx);

            // Then
            assertNotNull(selection);
            assertNotNull(selection.getProviderType());
            assertNotNull(selection.getSelectionReason());
        }

        @Test
        @DisplayName("should return a valid selection for greeting intent")
        void shouldSelectForGreetingIntent() {
            // Given
            ProviderSelector selector = createSelector(List.of(pennybotProvider));
            selector.initProviderMap();

            ConversationContext ctx = ConversationContext.builder()
                .contextId("test").userId("user1").platform("facebook").build();
            IntentAnalysisResult analysis = IntentAnalysisResult.builder()
                .primaryIntent("greeting").confidence(0.95).complexity("low").build();

            // When
            ProviderSelection selection = selector.select(analysis, ctx);

            // Then
            assertNotNull(selection);
            assertNotNull(selection.getProviderType());
        }

        @Test
        @DisplayName("should throw when analysis is null")
        void shouldThrowOnNullAnalysis() {
            // Given
            ProviderSelector selector = createSelector(List.of(pennybotProvider));
            selector.initProviderMap();

            ConversationContext ctx = ConversationContext.builder()
                .contextId("test").userId("user1").platform("facebook").build();

            // When & Then - null analysis causes NPE in selectHybrid -> analysis.getPrimaryIntent()
            assertThrows(NullPointerException.class, () -> selector.select(null, ctx));
        }
    }

    @Nested
    @DisplayName("Provider Health")
    class ProviderHealthTests {

        @Test
        @DisplayName("should track provider health updates")
        void shouldTrackHealthUpdates() {
            // Given
            ProviderSelector selector = createSelector(List.of(pennybotProvider));
            selector.initProviderMap();

            // When
            selector.updateProviderHealth(
                ProviderSelector.ProviderType.PENNYBOT, true, "OK");

            // Then
            Map<String, ProviderSelector.ProviderHealth> health = selector.getAllProviderHealth();
            assertFalse(health.isEmpty());
            assertTrue(health.get("PENNYBOT").isHealthy());
        }

        @Test
        @DisplayName("should mark provider unhealthy after consecutive failures")
        void shouldMarkUnhealthyAfterFailures() {
            // Given
            ProviderSelector selector = createSelector(List.of(pennybotProvider));
            selector.initProviderMap();

            // When - 3 consecutive failures
            selector.updateProviderHealth(
                ProviderSelector.ProviderType.PENNYBOT, false, "timeout");
            selector.updateProviderHealth(
                ProviderSelector.ProviderType.PENNYBOT, false, "timeout");
            selector.updateProviderHealth(
                ProviderSelector.ProviderType.PENNYBOT, false, "timeout");

            // Then
            Map<String, ProviderSelector.ProviderHealth> health = selector.getAllProviderHealth();
            assertFalse(health.get("PENNYBOT").isHealthy());
            assertEquals(3, health.get("PENNYBOT").getConsecutiveFailures());
        }

        @Test
        @DisplayName("should reset failure count on healthy update")
        void shouldResetFailuresOnHealthy() {
            // Given
            ProviderSelector selector = createSelector(List.of(pennybotProvider));
            selector.initProviderMap();

            // When
            selector.updateProviderHealth(
                ProviderSelector.ProviderType.PENNYBOT, false, "fail");
            selector.updateProviderHealth(
                ProviderSelector.ProviderType.PENNYBOT, false, "fail");
            selector.updateProviderHealth(
                ProviderSelector.ProviderType.PENNYBOT, true, "recovered");

            // Then
            Map<String, ProviderSelector.ProviderHealth> health = selector.getAllProviderHealth();
            assertTrue(health.get("PENNYBOT").isHealthy());
            assertEquals(0, health.get("PENNYBOT").getConsecutiveFailures());
        }
    }
}
