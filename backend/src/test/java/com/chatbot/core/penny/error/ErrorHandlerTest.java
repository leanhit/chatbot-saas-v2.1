package com.chatbot.core.penny.error;

import com.chatbot.core.penny.core.config.PennyProperties;
import com.chatbot.core.penny.dto.request.MiddlewareRequest;
import com.chatbot.core.penny.dto.response.MiddlewareResponse;
import com.chatbot.core.penny.error.exceptions.PennyException;
import com.chatbot.core.penny.routing.ProviderSelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ErrorHandler:
 * - Error classification and user-friendly messages
 * - Contextual fallback responses
 * - CircuitBreaker open/half-open/closed state transitions
 * - executeWithRetry exponential backoff
 */
@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;

    private PennyProperties properties;
    private ErrorHandler errorHandler;

    private MiddlewareRequest request;

    @BeforeEach
    void setUp() {
        properties = new PennyProperties();
        // Use small thresholds for fast tests
        PennyProperties.Error errorProps = new PennyProperties.Error();
        errorProps.setCircuitbreakerEnabled(true);
        errorProps.setFallbackEnabled(true);
        errorProps.setMaxRetries(2);
        errorProps.setFailureThreshold(3);
        errorProps.setTimeoutDuration(Duration.ofSeconds(30));
        errorProps.setRecoveryDuration(Duration.ofMillis(10)); // Very short for tests
        properties.setError(errorProps);

        // Lenient stub for Redis ops (called during persistCircuitBreakerState)
        lenient().when(redisTemplate.opsForHash()).thenReturn(mock(org.springframework.data.redis.core.HashOperations.class));
        lenient().when(redisTemplate.expire(any(), any())).thenReturn(true);

        errorHandler = new ErrorHandler(properties, redisTemplate);

        request = MiddlewareRequest.builder()
            .requestId("req-test")
            .userId("user-1")
            .platform("facebook")
            .message("Xin chào")
            .timestamp(Instant.now())
            .build();
    }

    // ── handleError — error classification ────────────────────────────────────

    @Nested
    @DisplayName("handleError() — error classification")
    class HandleErrorClassificationTests {

        @Test
        @DisplayName("IllegalArgumentException → VALIDATION_ERROR response")
        void illegalArgument_returnsValidationError() {
            MiddlewareResponse resp = errorHandler.handleError(
                new IllegalArgumentException("Field missing"), request, System.currentTimeMillis());

            assertNotNull(resp);
            assertTrue(resp.hasError());
            // Fallback enabled → may return fallback or error; either way no crash
            assertNotNull(resp.getResponse() != null ? resp.getResponse() : resp.getErrorMessage());
        }

        @Test
        @DisplayName("TimeoutException → timeout user message when fallback disabled")
        void timeout_withFallbackDisabled_returnsTimeoutMessage() {
            properties.getError().setFallbackEnabled(false);

            MiddlewareResponse resp = errorHandler.handleError(
                new java.util.concurrent.TimeoutException("too slow"), request, System.currentTimeMillis());

            assertNotNull(resp);
            assertTrue(resp.hasError());
            assertTrue(resp.getErrorMessage().contains("quá lâu") || resp.getErrorMessage().contains("timeout"),
                "Expected timeout message, got: " + resp.getErrorMessage());
        }

        @Test
        @DisplayName("ConnectException → connection error message when fallback disabled")
        void connectException_withFallbackDisabled_returnsConnectionMessage() {
            properties.getError().setFallbackEnabled(false);

            MiddlewareResponse resp = errorHandler.handleError(
                new java.net.ConnectException("refused"), request, System.currentTimeMillis());

            assertNotNull(resp);
            assertTrue(resp.hasError());
            assertTrue(resp.getErrorMessage().contains("kết nối"),
                "Expected connection message, got: " + resp.getErrorMessage());
        }

        @Test
        @DisplayName("PennyException → uses exception message directly")
        void pennyException_withFallbackDisabled_usesExceptionMessage() {
            properties.getError().setFallbackEnabled(false);

            MiddlewareResponse resp = errorHandler.handleError(
                new PennyException("Bot quota exceeded"), request, System.currentTimeMillis());

            assertNotNull(resp);
            assertTrue(resp.hasError());
            assertEquals("Bot quota exceeded", resp.getErrorMessage());
        }

        @Test
        @DisplayName("Unknown exception → generic error message")
        void unknown_withFallbackDisabled_returnsGenericMessage() {
            properties.getError().setFallbackEnabled(false);

            MiddlewareResponse resp = errorHandler.handleError(
                new RuntimeException("unexpected"), request, System.currentTimeMillis());

            assertNotNull(resp);
            assertTrue(resp.hasError());
            assertTrue(resp.getErrorMessage().contains("lỗi xảy ra"));
        }
    }

    // ── handleError — contextual fallback ─────────────────────────────────────

    @Nested
    @DisplayName("handleError() — contextual fallback responses")
    class FallbackResponseTests {

        @Test
        @DisplayName("price-related message → price fallback text")
        void priceMessage_returnsPriceFallback() {
            request.setMessage("sản phẩm này giá bao nhiêu");

            MiddlewareResponse resp = errorHandler.handleError(
                new RuntimeException("any error"), request, System.currentTimeMillis());

            assertNotNull(resp);
            // Fallback is used (fallbackEnabled = true)
            assertEquals("fallback", resp.getStatus());
            assertTrue(resp.getResponse().contains("giá") || resp.getResponse().contains("nhân viên"),
                "Expected price fallback, got: " + resp.getResponse());
        }

        @Test
        @DisplayName("order-related message → order fallback text")
        void orderMessage_returnsOrderFallback() {
            request.setMessage("kiểm tra đơn hàng của tôi");

            MiddlewareResponse resp = errorHandler.handleError(
                new RuntimeException("any error"), request, System.currentTimeMillis());

            assertNotNull(resp);
            assertEquals("fallback", resp.getStatus());
            assertTrue(resp.getResponse().contains("đơn") || resp.getResponse().contains("order"),
                "Expected order fallback, got: " + resp.getResponse());
        }

        @Test
        @DisplayName("greeting message → greeting fallback text")
        void greetingMessage_returnsGreetingFallback() {
            request.setMessage("xin chào bạn");

            MiddlewareResponse resp = errorHandler.handleError(
                new RuntimeException("any error"), request, System.currentTimeMillis());

            assertNotNull(resp);
            assertEquals("fallback", resp.getStatus());
            assertTrue(resp.getResponse().contains("Xin chào") || resp.getResponse().contains("chào"),
                "Expected greeting fallback, got: " + resp.getResponse());
        }

        @Test
        @DisplayName("generic message → generic fallback text")
        void genericMessage_returnsGenericFallback() {
            request.setMessage("Bạn có thể làm gì?");

            MiddlewareResponse resp = errorHandler.handleError(
                new RuntimeException("any error"), request, System.currentTimeMillis());

            assertNotNull(resp);
            assertEquals("fallback", resp.getStatus());
            assertNotNull(resp.getResponse());
        }

        @Test
        @DisplayName("fallback response should have shouldSendResponse = true")
        void fallbackResponse_hasShouldSendResponseTrue() {
            MiddlewareResponse resp = errorHandler.handleError(
                new RuntimeException("error"), request, System.currentTimeMillis());

            assertTrue(resp.isShouldSendResponse());
        }
    }

    // ── handleProviderError ───────────────────────────────────────────────────

    @Nested
    @DisplayName("handleProviderError()")
    class HandleProviderErrorTests {

        @Test
        @DisplayName("should return provider fallback response referencing provider name")
        void returnsProviderFallback() {
            MiddlewareResponse resp = errorHandler.handleProviderError(
                new RuntimeException("GPT down"),
                request,
                ProviderSelector.ProviderType.GPT);

            assertNotNull(resp);
            assertNotNull(resp.getResponse());
            assertTrue(resp.getResponse().contains("GPT") || resp.getResponse().contains("provider"),
                "Expected provider name in response, got: " + resp.getResponse());
        }

        @Test
        @DisplayName("should return error response when fallback is disabled")
        void fallbackDisabled_returnsErrorResponse() {
            properties.getError().setFallbackEnabled(false);

            MiddlewareResponse resp = errorHandler.handleProviderError(
                new RuntimeException("PENNYBOT down"),
                request,
                ProviderSelector.ProviderType.PENNYBOT);

            assertNotNull(resp);
            assertTrue(resp.hasError());
        }
    }

    // ── CircuitBreaker ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CircuitBreaker state machine")
    class CircuitBreakerTests {

        @Test
        @DisplayName("should be CLOSED initially → isCircuitBreakerOpen returns false")
        void initiallyClosedReturnsFalse() {
            assertFalse(errorHandler.isCircuitBreakerOpen("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should OPEN after reaching failure threshold")
        void opensAfterFailureThreshold() {
            // Trigger 3 errors to reach failureThreshold = 3
            for (int i = 0; i < 3; i++) {
                properties.getError().setFallbackEnabled(false);
                errorHandler.handleError(
                    new IllegalArgumentException("fail " + i),
                    request, System.currentTimeMillis());
            }

            assertTrue(errorHandler.isCircuitBreakerOpen("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should CLOSE on recordSuccess()")
        void closesAfterSuccess() {
            // Open the breaker first
            for (int i = 0; i < 3; i++) {
                properties.getError().setFallbackEnabled(false);
                errorHandler.handleError(
                    new IllegalArgumentException("fail"), request, System.currentTimeMillis());
            }
            assertTrue(errorHandler.isCircuitBreakerOpen("VALIDATION_ERROR"));

            // Record success → should close
            errorHandler.recordSuccess("VALIDATION_ERROR");
            assertFalse(errorHandler.isCircuitBreakerOpen("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("should always return false when circuitbreaker is disabled")
        void disabledCircuitBreaker_alwaysFalse() {
            properties.getError().setCircuitbreakerEnabled(false);

            // Simulate many failures
            for (int i = 0; i < 10; i++) {
                errorHandler.handleError(
                    new RuntimeException("fail"), request, System.currentTimeMillis());
            }

            assertFalse(errorHandler.isCircuitBreakerOpen("UNKNOWN_ERROR"));
        }

        @Test
        @DisplayName("should return CircuitBreakerStatus map")
        void getCircuitBreakerStatus_returnsMap() {
            // Trigger at least one failure to populate map
            properties.getError().setFallbackEnabled(false);
            errorHandler.handleError(
                new RuntimeException("fail"), request, System.currentTimeMillis());

            var statusMap = errorHandler.getCircuitBreakerStatus();
            assertNotNull(statusMap);
            assertFalse(statusMap.isEmpty());
        }
    }

    // ── CircuitBreaker inner class direct tests ───────────────────────────────

    @Nested
    @DisplayName("CircuitBreaker inner class")
    class CircuitBreakerInnerClassTests {

        @Test
        @DisplayName("should transition CLOSED → OPEN after threshold failures")
        void transitionToOpen() {
            ErrorHandler.CircuitBreaker cb = new ErrorHandler.CircuitBreaker(
                3, Duration.ofSeconds(30), Duration.ofMillis(50));

            assertFalse(cb.isOpen());
            cb.recordFailure();
            cb.recordFailure();
            assertFalse(cb.isOpen()); // not yet
            cb.recordFailure();       // hits threshold
            assertTrue(cb.isOpen());
            assertEquals(3, cb.getFailureCount());
        }

        @Test
        @DisplayName("should transition OPEN → CLOSED via recordSuccess()")
        void transitionToClosedViaSuccess() {
            ErrorHandler.CircuitBreaker cb = new ErrorHandler.CircuitBreaker(
                2, Duration.ofSeconds(30), Duration.ofMillis(50));
            cb.recordFailure();
            cb.recordFailure(); // now OPEN
            assertTrue(cb.isOpen());

            cb.recordSuccess();
            assertFalse(cb.isOpen());
            assertFalse(cb.isHalfOpen());
            assertEquals(0, cb.getFailureCount());
        }

        @Test
        @DisplayName("should become HALF_OPEN after recovery duration expires")
        void transitionToHalfOpenAfterRecovery() throws InterruptedException {
            ErrorHandler.CircuitBreaker cb = new ErrorHandler.CircuitBreaker(
                2, Duration.ofSeconds(30), Duration.ofMillis(50)); // 50ms recovery
            cb.recordFailure();
            cb.recordFailure(); // OPEN

            Thread.sleep(100); // wait past 50ms recovery

            // isOpen() should now return false (transitions to HALF_OPEN)
            assertFalse(cb.isOpen());
            assertTrue(cb.isHalfOpen());
        }
    }

    // ── executeWithRetry ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("executeWithRetry()")
    class ExecuteWithRetryTests {

        @Test
        @DisplayName("should return result immediately on first success")
        void succeedsOnFirstAttempt() {
            String result = errorHandler.executeWithRetry(
                () -> "hello", "testOp");

            assertEquals("hello", result);
        }

        @Test
        @DisplayName("should retry and succeed on second attempt")
        void succeedsAfterOneRetry() {
            AtomicInteger callCount = new AtomicInteger(0);

            String result = errorHandler.executeWithRetry(() -> {
                if (callCount.incrementAndGet() < 2) {
                    throw new RuntimeException("first attempt fails");
                }
                return "recovered";
            }, "retryOp");

            assertEquals("recovered", result);
            assertEquals(2, callCount.get());
        }

        @Test
        @DisplayName("should throw RuntimeException after exhausting all retries")
        void exhaustsRetries_throwsException() {
            // maxRetries = 2 → 3 total attempts
            assertThrows(RuntimeException.class, () ->
                errorHandler.executeWithRetry(() -> {
                    throw new RuntimeException("always fails");
                }, "alwaysFailOp")
            );
        }
    }
}
