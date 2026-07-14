package com.chatbot.shared.penny.core;

import com.chatbot.shared.penny.analytics.AnalyticsCollector;
import com.chatbot.shared.penny.context.ContextManager;
import com.chatbot.shared.penny.context.ConversationContext;
import com.chatbot.shared.penny.dto.request.MiddlewareRequest;
import com.chatbot.shared.penny.dto.response.MiddlewareResponse;
import com.chatbot.shared.penny.error.ErrorHandler;
import com.chatbot.shared.penny.routing.ProviderSelector;
import com.chatbot.shared.penny.routing.dto.IntentAnalysisResult;
import com.chatbot.shared.penny.routing.dto.ProviderSelection;
import com.chatbot.shared.penny.rules.CustomLogicEngine;
import com.chatbot.shared.penny.service.IntentAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PennyMiddlewareEngine - validates that validateRequest()
 * is now called and rejects invalid requests properly.
 */
@ExtendWith(MockitoExtension.class)
class PennyMiddlewareEngineTest {

    @Mock private ContextManager contextManager;
    @Mock private IntentAnalyzer intentAnalyzer;
    @Mock private ProviderSelector providerSelector;
    @Mock private ErrorHandler errorHandler;
    @Mock private AnalyticsCollector analyticsCollector;
    @Mock private CustomLogicEngine customLogicEngine;

    private PennyMiddlewareEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PennyMiddlewareEngine(
            contextManager, intentAnalyzer, providerSelector,
            errorHandler, analyticsCollector, customLogicEngine);

        // ErrorHandler should always return an error MiddlewareResponse for any exception
        lenient().when(errorHandler.handleError(any(Exception.class), any(), anyLong()))
            .thenAnswer(invocation -> {
                Exception ex = invocation.getArgument(0);
                return MiddlewareResponse.error("error-req", ex.getMessage(), "VALIDATION_ERROR");
            });
    }

    @Test
    @DisplayName("should reject request with null message")
    void shouldRejectNullMessage() {
        // Given - request without message
        MiddlewareRequest request = MiddlewareRequest.builder()
            .requestId("req-1")
            .userId("user-1")
            .platform("facebook")
            .message(null)
            .timestamp(Instant.now())
            .build();

        // When
        MiddlewareResponse response = engine.processMessage(request);

        // Then
        assertNotNull(response);
        assertTrue(response.hasError());
        assertTrue(response.getErrorMessage().contains("Message"));
        // validateRequest should have been triggered, leading to errorHandler
        verify(errorHandler).handleError(any(IllegalArgumentException.class), eq(request), anyLong());
    }

    @Test
    @DisplayName("should reject request with empty message")
    void shouldRejectEmptyMessage() {
        // Given
        MiddlewareRequest request = MiddlewareRequest.builder()
            .requestId("req-2")
            .userId("user-1")
            .platform("facebook")
            .message("   ")
            .timestamp(Instant.now())
            .build();

        // When
        MiddlewareResponse response = engine.processMessage(request);

        // Then
        assertNotNull(response);
        assertTrue(response.hasError());
        verify(errorHandler).handleError(any(IllegalArgumentException.class), eq(request), anyLong());
    }

    @Test
    @DisplayName("should reject request with null userId")
    void shouldRejectNullUserId() {
        // Given
        MiddlewareRequest request = MiddlewareRequest.builder()
            .requestId("req-3")
            .userId(null)
            .platform("facebook")
            .message("Hello")
            .timestamp(Instant.now())
            .build();

        // When
        MiddlewareResponse response = engine.processMessage(request);

        // Then
        assertNotNull(response);
        assertTrue(response.hasError());
        verify(errorHandler).handleError(any(IllegalArgumentException.class), eq(request), anyLong());
    }

    @Test
    @DisplayName("should reject request with null platform")
    void shouldRejectNullPlatform() {
        // Given
        MiddlewareRequest request = MiddlewareRequest.builder()
            .requestId("req-4")
            .userId("user-1")
            .platform(null)
            .message("Hello")
            .timestamp(Instant.now())
            .build();

        // When
        MiddlewareResponse response = engine.processMessage(request);

        // Then
        assertNotNull(response);
        assertTrue(response.hasError());
        verify(errorHandler).handleError(any(IllegalArgumentException.class), eq(request), anyLong());
    }

    @Test
    @DisplayName("should process valid request successfully")
    void shouldProcessValidRequest() {
        // Given
        MiddlewareRequest request = MiddlewareRequest.builder()
            .requestId("req-5")
            .userId("user-1")
            .platform("facebook")
            .message("Xin chào")
            .timestamp(Instant.now())
            .build();

        ConversationContext ctx = ConversationContext.builder()
            .contextId("ctx-1")
            .userId("user-1")
            .platform("facebook")
            .build();

        IntentAnalysisResult analysis = IntentAnalysisResult.builder()
            .primaryIntent("greeting")
            .confidence(0.9)
            .complexity("low")
            .build();

        ProviderSelection selection = ProviderSelection.builder()
            .providerType(ProviderSelector.ProviderType.PENNYBOT)
            .selectionReason("default")
            .confidence(0.8)
            .build();

        when(contextManager.loadContext(any())).thenReturn(ctx);
        when(intentAnalyzer.analyze(any(), any())).thenReturn(analysis);
        when(customLogicEngine.processWithCustomLogic(any(), any(), any())).thenReturn(null);
        when(providerSelector.select(any(), any())).thenReturn(selection);

        // When
        MiddlewareResponse response = engine.processMessage(request);

        // Then
        assertNotNull(response);
        // Should have gone through intent analysis and provider selection
        verify(contextManager).loadContext(any());
        verify(intentAnalyzer).analyze(any(), any());
        verify(providerSelector).select(any(), any());
        // ErrorHandler should NOT have been called for valid request
        verify(errorHandler, never()).handleError(
            any(IllegalArgumentException.class), any(), anyLong());
    }
}
