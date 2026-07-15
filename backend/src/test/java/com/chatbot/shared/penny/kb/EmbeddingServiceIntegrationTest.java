package com.chatbot.shared.penny.kb;

import com.chatbot.shared.penny.core.config.PennyProperties;
import com.chatbot.shared.penny.security.ApiKeyManager;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration test for EmbeddingService
 * This test verifies service initialization and configuration in a Spring context
 */
@SpringBootTest(classes = {EmbeddingService.class})
@EnableConfigurationProperties(PennyProperties.class)
@ActiveProfiles("test")
class EmbeddingServiceIntegrationTest {

    @Autowired
    private EmbeddingService embeddingService;

    @MockBean
    private ApiKeyManager apiKeyManager;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockBean
    private RetryRegistry retryRegistry;

    @MockBean
    private CircuitBreaker circuitBreaker;

    @MockBean
    private Retry retry;

    @BeforeEach
    void setUp() {
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        when(retryRegistry.retry(anyString())).thenReturn(retry);
    }

    @Test
    void testEmbeddingService_WithApiKey_ShouldBeEnabled() {
        // Arrange
        when(apiKeyManager.getOpenAiApiKey()).thenReturn("sk-test-key");
        
        // Act
        embeddingService.init();
        
        // Assert
        assertTrue(embeddingService.isEnabled());
    }

    @Test
    void testEmbeddingService_WithoutApiKey_ShouldBeDisabled() {
        // Arrange
        when(apiKeyManager.getOpenAiApiKey()).thenReturn(null);
        
        // Act
        embeddingService.init();
        
        // Assert
        assertFalse(embeddingService.isEnabled());
    }
}
