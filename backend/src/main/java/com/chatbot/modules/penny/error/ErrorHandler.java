package com.chatbot.modules.penny.error;

import com.chatbot.modules.penny.core.config.PennyProperties;
import com.chatbot.modules.penny.dto.request.MiddlewareRequest;
import com.chatbot.modules.penny.dto.response.MiddlewareResponse;
import com.chatbot.modules.penny.error.exceptions.PennyException;
import com.chatbot.modules.penny.routing.ProviderSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Error Handler - Xử lý lỗi và fallback strategies
 */
@Service
@Slf4j
public class ErrorHandler {
    
    private final PennyProperties properties;
    private final Map<String, CircuitBreaker> circuitBreakers;
    
    public ErrorHandler(PennyProperties properties) {
        this.properties = properties;
        this.circuitBreakers = new HashMap<>();
    }
    
    /**
     * Handle processing error
     */
    public MiddlewareResponse handleError(Exception e, MiddlewareRequest request, long startTime) {
        String errorType = classifyError(e);
        long processingTime = System.currentTimeMillis() - startTime;
        
        log.error("❌ Handling error [{}] for request {}: {}", 
            errorType, request.getRequestId(), e.getMessage(), e);
        
        // Update circuit breaker if enabled
        if (properties.getError().isCircuitbreakerEnabled()) {
            updateCircuitBreaker(errorType, false);
        }
        
        // Try fallback if enabled
        if (properties.getError().isFallbackEnabled()) {
            MiddlewareResponse fallbackResponse = tryFallback(request, e);
            if (fallbackResponse != null) {
                log.info("🔄 Fallback response generated for request: {}", request.getRequestId());
                return fallbackResponse;
            }
        }
        
        // Generate error response
        return MiddlewareResponse.error(
            request.getRequestId(),
            generateErrorMessage(e),
            errorType
        );
    }
    
    /**
     * Handle provider-specific error
     */
    public MiddlewareResponse handleProviderError(Exception e, 
                                               MiddlewareRequest request, 
                                               ProviderSelector.ProviderType providerType) {
        String errorKey = providerType.toString();
        
        log.error("❌ Provider error [{}] for request {}: {}", 
            providerType, request.getRequestId(), e.getMessage());
        
        // Update circuit breaker for this provider
        if (properties.getError().isCircuitbreakerEnabled()) {
            CircuitBreaker cb = circuitBreakers.computeIfAbsent(errorKey, k -> new CircuitBreaker(
                properties.getError().getFailureThreshold(),
                properties.getError().getTimeoutDuration(),
                properties.getError().getRecoveryDuration()
            ));
            cb.recordFailure();
        }
        
        // Try fallback provider
        if (properties.getError().isFallbackEnabled()) {
            return tryProviderFallback(request, providerType, e);
        }
        
        // Generate error response
        return MiddlewareResponse.error(
            request.getRequestId(),
            String.format("Xin lỗi, có lỗi xảy ra với provider %s. Vui lòng thử lại sau.", providerType),
            "PROVIDER_ERROR"
        );
    }
    
    /**
     * Check if circuit breaker is open for provider
     */
    public boolean isCircuitBreakerOpen(String providerType) {
        if (!properties.getError().isCircuitbreakerEnabled()) {
            return false;
        }
        
        CircuitBreaker cb = circuitBreakers.get(providerType);
        return cb != null && cb.isOpen();
    }
    
    /**
     * Record success for circuit breaker
     */
    public void recordSuccess(String providerType) {
        if (!properties.getError().isCircuitbreakerEnabled()) {
            return;
        }
        
        CircuitBreaker cb = circuitBreakers.get(providerType);
        if (cb != null) {
            cb.recordSuccess();
        }
    }
    
    /**
     * Get circuit breaker status
     */
    public Map<String, CircuitBreakerStatus> getCircuitBreakerStatus() {
        Map<String, CircuitBreakerStatus> status = new HashMap<>();
        
        circuitBreakers.forEach((key, cb) -> {
            CircuitBreakerStatus cbStatus = new CircuitBreakerStatus();
            cbStatus.setOpen(cb.isOpen());
            cbStatus.setHalfOpen(cb.isHalfOpen());
            cbStatus.setFailureCount(cb.getFailureCount());
            cbStatus.setLastFailureTime(cb.getLastFailureTime());
            cbStatus.setNextAttemptTime(cb.getNextAttemptTime());
            
            status.put(key, cbStatus);
        });
        
        return status;
    }
    
    // Private helper methods
    
    private String classifyError(Exception e) {
        if (e instanceof java.util.concurrent.TimeoutException) {
            return "TIMEOUT";
        } else if (e instanceof java.net.ConnectException) {
            return "CONNECTION_ERROR";
        } else if (e instanceof java.net.SocketTimeoutException) {
            return "SOCKET_TIMEOUT";
        } else if (e instanceof org.springframework.web.client.HttpClientErrorException) {
            return "HTTP_CLIENT_ERROR";
        } else if (e instanceof org.springframework.web.client.HttpServerErrorException) {
            return "HTTP_SERVER_ERROR";
        } else if (e instanceof com.fasterxml.jackson.core.JsonProcessingException) {
            return "JSON_PROCESSING_ERROR";
        } else if (e instanceof IllegalArgumentException) {
            return "VALIDATION_ERROR";
        } else if (e instanceof PennyException) {
            return "PENNY_ERROR";
        } else {
            return "UNKNOWN_ERROR";
        }
    }
    
    private String generateErrorMessage(Exception e) {
        String errorType = classifyError(e);
        
        switch (errorType) {
            case "TIMEOUT":
                return "Xin lỗi, yêu cầu xử lý quá lâu. Vui lòng thử lại sau.";
            case "CONNECTION_ERROR":
            case "SOCKET_TIMEOUT":
                return "Xin lỗi, không thể kết nối đến dịch vụ. Vui lòng thử lại sau.";
            case "HTTP_CLIENT_ERROR":
                return "Xin lỗi, yêu cầu không hợp lệ. Vui lòng kiểm tra lại.";
            case "HTTP_SERVER_ERROR":
                return "Xin lỗi, có lỗi từ phía máy chủ. Vui lòng thử lại sau.";
            case "JSON_PROCESSING_ERROR":
                return "Xin lỗi, có lỗi xử lý dữ liệu. Vui lòng thử lại.";
            case "VALIDATION_ERROR":
                return "Xin lỗi, thông tin yêu cầu không hợp lệ.";
            case "PENNY_ERROR":
                return e.getMessage();
            default:
                return "Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau.";
        }
    }
    
    private MiddlewareResponse tryFallback(MiddlewareRequest request, Exception originalError) {
        try {
            log.debug("🔄 Attempting fallback for request: {}", request.getRequestId());
            
            // Simple fallback response
            String fallbackMessage = generateFallbackMessage(request);
            
            return MiddlewareResponse.builder()
                .requestId(request.getRequestId())
                .response(fallbackMessage)
                .providerUsed("FALLBACK")
                .status("fallback")
                .errorMessage("Original error: " + originalError.getMessage())
                .timestamp(Instant.now())
                .shouldSendResponse(true)
                .build();
                
        } catch (Exception e) {
            log.error("❌ Fallback also failed for request {}: {}", 
                request.getRequestId(), e.getMessage(), e);
            return null;
        }
    }
    
    private MiddlewareResponse tryProviderFallback(MiddlewareRequest request, 
                                                 ProviderSelector.ProviderType failedProvider, 
                                                 Exception originalError) {
        try {
            log.debug("🔄 Attempting provider fallback from {} for request: {}", 
                failedProvider, request.getRequestId());
            
            // In real implementation, this would try alternative providers
            // For now, return a simple fallback response
            
            String fallbackMessage = String.format(
                "Xin lỗi, provider %s đang gặp sự cố. Chúng tôi sẽ chuyển bạn đến provider khác.",
                failedProvider
            );
            
            return MiddlewareResponse.builder()
                .requestId(request.getRequestId())
                .response(fallbackMessage)
                .providerUsed("FALLBACK")
                .status("provider_fallback")
                .errorMessage("Original error: " + originalError.getMessage())
                .timestamp(Instant.now())
                .shouldSendResponse(true)
                .build();
                
        } catch (Exception e) {
            log.error("❌ Provider fallback also failed for request {}: {}", 
                request.getRequestId(), e.getMessage(), e);
            return null;
        }
    }
    
    private String generateFallbackMessage(MiddlewareRequest request) {
        // Generate contextual fallback message based on request
        if (request.getMessage() != null) {
            String message = request.getMessage().toLowerCase();
            
            if (message.contains("giá") || message.contains("bao nhiêu")) {
                return "Xin lỗi, tôi không thể kiểm tra giá ngay lúc này. Vui lòng liên hệ nhân viên hỗ trợ.";
            } else if (message.contains("đơn") || message.contains("order")) {
                return "Xin lỗi, tôi không thể kiểm tra đơn hàng ngay lúc này. Vui lòng thử lại sau hoặc liên hệ hỗ trợ.";
            } else if (message.contains("chào") || message.contains("hello")) {
                return "Xin chào! Hiện tại hệ thống đang gặp sự cố kỹ thuật. Vui lòng thử lại sau.";
            }
        }
        
        return "Xin lỗi, hệ thống đang gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ nhân viên hỗ trợ.";
    }
    
    private void updateCircuitBreaker(String errorType, boolean success) {
        CircuitBreaker cb = circuitBreakers.computeIfAbsent(errorType, k -> new CircuitBreaker(
            properties.getError().getFailureThreshold(),
            properties.getError().getTimeoutDuration(),
            properties.getError().getRecoveryDuration()
        ));
        
        if (success) {
            cb.recordSuccess();
        } else {
            cb.recordFailure();
        }
    }
    
    // Inner classes
    
    public static class CircuitBreaker {
        private final int failureThreshold;
        private final java.time.Duration timeoutDuration;
        private final java.time.Duration recoveryDuration;
        
        private int failureCount = 0;
        private Instant lastFailureTime;
        private Instant nextAttemptTime;
        private State state = State.CLOSED;
        
        public CircuitBreaker(int failureThreshold, 
                             java.time.Duration timeoutDuration,
                             java.time.Duration recoveryDuration) {
            this.failureThreshold = failureThreshold;
            this.timeoutDuration = timeoutDuration;
            this.recoveryDuration = recoveryDuration;
        }
        
        public synchronized void recordSuccess() {
            failureCount = 0;
            state = State.CLOSED;
            lastFailureTime = null;
            nextAttemptTime = null;
        }
        
        public synchronized void recordFailure() {
            failureCount++;
            lastFailureTime = Instant.now();
            
            if (failureCount >= failureThreshold) {
                state = State.OPEN;
                nextAttemptTime = lastFailureTime.plus(recoveryDuration);
            }
        }
        
        public synchronized boolean isOpen() {
            if (state == State.OPEN) {
                if (Instant.now().isAfter(nextAttemptTime)) {
                    state = State.HALF_OPEN;
                    return false;
                }
                return true;
            }
            return false;
        }
        
        public synchronized boolean isHalfOpen() {
            return state == State.HALF_OPEN;
        }
        
        public int getFailureCount() {
            return failureCount;
        }
        
        public Instant getLastFailureTime() {
            return lastFailureTime;
        }
        
        public Instant getNextAttemptTime() {
            return nextAttemptTime;
        }
        
        private enum State {
            CLOSED, OPEN, HALF_OPEN
        }
    }
    
    public static class CircuitBreakerStatus {
        private boolean isOpen;
        private boolean isHalfOpen;
        private int failureCount;
        private Instant lastFailureTime;
        private Instant nextAttemptTime;
        
        public CircuitBreakerStatus() {}
        
        // Getters and setters
        public boolean isOpen() { return isOpen; }
        public void setOpen(boolean open) { this.isOpen = open; }
        
        public boolean isHalfOpen() { return isHalfOpen; }
        public void setHalfOpen(boolean halfOpen) { this.isHalfOpen = halfOpen; }
        
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        
        public Instant getLastFailureTime() { return lastFailureTime; }
        public void setLastFailureTime(Instant lastFailureTime) { this.lastFailureTime = lastFailureTime; }
        
        public Instant getNextAttemptTime() { return nextAttemptTime; }
        public void setNextAttemptTime(Instant nextAttemptTime) { this.nextAttemptTime = nextAttemptTime; }
    }
}
