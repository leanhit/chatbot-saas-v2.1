package com.chatbot.shared.penny.kb;

import com.chatbot.shared.penny.security.ApiKeyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ApiKeyManager apiKeyManager;

    @InjectMocks
    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(embeddingService, "embeddingModel", "text-embedding-3-small");
        ReflectionTestUtils.setField(embeddingService, "cacheTtlDays", 7);
    }

    @Test
    void testInit_WithValidApiKey_ShouldEnableService() {
        // Arrange
        when(apiKeyManager.getOpenAiApiKey()).thenReturn("sk-test-key");

        // Act
        embeddingService.init();

        // Assert
        assertTrue(embeddingService.isEnabled());
    }

    @Test
    void testInit_WithInvalidApiKey_ShouldDisableService() {
        // Arrange
        when(apiKeyManager.getOpenAiApiKey()).thenReturn(null);

        // Act
        embeddingService.init();

        // Assert
        assertFalse(embeddingService.isEnabled());
    }

    @Test
    void testGenerateEmbedding_WhenDisabled_ShouldReturnNull() {
        // Arrange
        when(apiKeyManager.getOpenAiApiKey()).thenReturn(null);
        embeddingService.init();

        // Act
        float[] result = embeddingService.generateEmbedding("test text");

        // Assert
        assertNull(result);
    }

    @Test
    void testGenerateEmbedding_WithEmptyText_ShouldReturnNull() {
        // Arrange
        when(apiKeyManager.getOpenAiApiKey()).thenReturn("sk-test-key");
        embeddingService.init();

        // Act
        float[] result = embeddingService.generateEmbedding("");

        // Assert
        assertNull(result);
    }

    @Test
    void testGenerateEmbedding_WithNullText_ShouldReturnNull() {
        // Arrange
        when(apiKeyManager.getOpenAiApiKey()).thenReturn("sk-test-key");
        embeddingService.init();

        // Act
        float[] result = embeddingService.generateEmbedding(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetEmbeddingModel_ShouldReturnConfiguredModel() {
        // Arrange
        ReflectionTestUtils.setField(embeddingService, "embeddingModel", "text-embedding-3-large");

        // Act
        String model = embeddingService.getEmbeddingModel();

        // Assert
        assertEquals("text-embedding-3-large", model);
    }

    @Test
    void testGetEmbeddingDimensions_WithTextEmbedding3Small_ShouldReturn1536() {
        // Arrange
        ReflectionTestUtils.setField(embeddingService, "embeddingModel", "text-embedding-3-small");

        // Act
        int dimensions = embeddingService.getEmbeddingDimensions();

        // Assert
        assertEquals(1536, dimensions);
    }

    @Test
    void testGetEmbeddingDimensions_WithTextEmbedding3Large_ShouldReturn3072() {
        // Arrange
        ReflectionTestUtils.setField(embeddingService, "embeddingModel", "text-embedding-3-large");

        // Act
        int dimensions = embeddingService.getEmbeddingDimensions();

        // Assert
        assertEquals(3072, dimensions);
    }

    @Test
    void testGetEmbeddingDimensions_WithUnknownModel_ShouldReturnDefault1536() {
        // Arrange
        ReflectionTestUtils.setField(embeddingService, "embeddingModel", "unknown-model");

        // Act
        int dimensions = embeddingService.getEmbeddingDimensions();

        // Assert
        assertEquals(1536, dimensions);
    }

    @Test
    void testGenerateEmbeddingsBatch_WithEmptyList_ShouldReturnEmptyList() {
        // Arrange
        when(apiKeyManager.getOpenAiApiKey()).thenReturn("sk-test-key");
        embeddingService.init();

        // Act
        var result = embeddingService.generateEmbeddingsBatch(java.util.List.of());

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testGenerateEmbeddingsBatch_WithNullList_ShouldReturnEmptyList() {
        // Arrange
        when(apiKeyManager.getOpenAiApiKey()).thenReturn("sk-test-key");
        embeddingService.init();

        // Act
        var result = embeddingService.generateEmbeddingsBatch(null);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testClearCache_ShouldCallRedisDelete() {
        // Arrange
        when(apiKeyManager.getOpenAiApiKey()).thenReturn("sk-test-key");
        embeddingService.init();

        // Act
        embeddingService.clearCache("test text");

        // Assert
        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    void testClearAllCache_ShouldCallRedisDeleteWithPattern() {
        // Arrange
        when(apiKeyManager.getOpenAiApiKey()).thenReturn("sk-test-key");
        when(redisTemplate.keys("embedding:*")).thenReturn(java.util.Set.of("embedding:123"));
        embeddingService.init();

        // Act
        embeddingService.clearAllCache();

        // Assert
        verify(redisTemplate, times(1)).delete(anyString());
    }
}
