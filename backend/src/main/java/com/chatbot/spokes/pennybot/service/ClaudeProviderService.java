package com.chatbot.spokes.pennybot.service;

import com.chatbot.shared.penny.providers.PromptTemplateService;
import com.chatbot.spokes.facebook.webhook.service.ChatbotProviderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Claude Provider Service — Tích hợp Anthropic Claude làm fallback AI provider
 *
 * Được kích hoạt khi penny.llm.anthropic.enabled=true.
 * Sử dụng Anthropic Messages API trực tiếp qua RestTemplate.
 * Vai trò: Fallback khi GPT down hoặc theo cấu hình tenant.
 */
@Service("claudeProviderService")
@ConditionalOnProperty(name = "penny.llm.anthropic.enabled", havingValue = "true")
@Slf4j
public class ClaudeProviderService implements ChatbotProviderService {

    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION  = "2023-06-01";

    @Value("${penny.llm.anthropic.api-key:}")
    private String apiKey;

    @Value("${penny.llm.anthropic.model:claude-3-haiku-20240307}")
    private String model;

    @Value("${penny.llm.anthropic.max-tokens:800}")
    private int maxTokens;

    private final PromptTemplateService promptTemplateService;
    private final RestTemplate restTemplate;

    private boolean clientReady = false;

    public ClaudeProviderService(PromptTemplateService promptTemplateService,
                                  RestTemplate restTemplate) {
        this.promptTemplateService = promptTemplateService;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("⚠️ Claude Provider: ANTHROPIC_API_KEY is not set. Provider will return fallback.");
            return;
        }
        this.clientReady = true;
        log.info("✅ Claude Provider initialized with model: {}", model);
    }

    @Override
    @CircuitBreaker(name = "claudeProvider", fallbackMethod = "sendMessageFallback")
    @Retry(name = "claudeProvider", fallbackMethod = "sendMessageFallback")
    public Map<String, Object> sendMessage(String botId, String senderId, String messageText) {
        log.info("🤖 [Claude] Processing message from {} via bot {}", senderId, botId);

        if (!clientReady) {
            return buildFallbackResponse(botId, senderId);
        }

        try {
            UUID botUuid = parseUUID(botId);
            String systemPrompt = promptTemplateService.buildSimpleSystemPrompt(botUuid);

            // Build Anthropic API request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("system", systemPrompt);
            requestBody.put("messages", List.of(
                Map.of("role", "user", "content", messageText)
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", ANTHROPIC_VERSION);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> apiResponse = restTemplate.postForObject(
                ANTHROPIC_API_URL, request, Map.class);

            String responseText = extractClaudeText(apiResponse);

            log.info("✅ [Claude] Response generated ({} chars), model: {}", responseText.length(), model);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("response", responseText);
            response.put("botId", botId);
            response.put("senderId", senderId);
            response.put("provider", "CLAUDE");
            response.put("model", model);
            response.put("timestamp", System.currentTimeMillis());
            return response;

        } catch (Exception e) {
            log.error("❌ [Claude] Error calling Anthropic API: {}", e.getMessage(), e);
            return buildErrorResponse(botId, senderId, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> sendEvent(String botId, String senderId,
                                          String eventName, Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Event acknowledged by Claude provider");
        response.put("eventName", eventName);
        response.put("processedAt", System.currentTimeMillis());
        return response;
    }

    @Override
    public boolean healthCheck(String botId) {
        if (!clientReady) return false;
        // Claude không có dedicated health endpoint — ping models list
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", ANTHROPIC_VERSION);
            // Gọi 1 request tối giản để kiểm tra connectivity
            Map<String, Object> pingBody = Map.of(
                "model", model,
                "max_tokens", 1,
                "messages", List.of(Map.of("role", "user", "content", "ping"))
            );
            restTemplate.postForObject(ANTHROPIC_API_URL,
                new HttpEntity<>(pingBody, headers), Map.class);
            log.debug("✅ [Claude] Health check passed");
            return true;
        } catch (Exception e) {
            log.warn("⚠️ [Claude] Health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderType() {
        return "CLAUDE";
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String extractClaudeText(Map<String, Object> apiResponse) {
        if (apiResponse == null) {
            return "Xin lỗi, không nhận được phản hồi từ AI.";
        }
        try {
            List<Map<String, Object>> content =
                (List<Map<String, Object>>) apiResponse.get("content");
            if (content != null && !content.isEmpty()) {
                Object text = content.get(0).get("text");
                if (text != null) return text.toString();
            }
        } catch (Exception e) {
            log.warn("⚠️ [Claude] Could not extract text from response: {}", e.getMessage());
        }
        return "Xin lỗi, có lỗi xảy ra khi xử lý phản hồi.";
    }

    private UUID parseUUID(String botId) {
        try {
            return UUID.fromString(botId);
        } catch (IllegalArgumentException e) {
            return new UUID(0, 0);
        }
    }

    /**
     * Fallback method for Circuit Breaker and Retry
     */
    public Map<String, Object> sendMessageFallback(String botId, String senderId, String messageText, Exception e) {
        log.warn("⚠️ [Claude] Circuit breaker opened or retry exhausted for bot: {}, error: {}", botId, e.getMessage());
        return buildCircuitBreakerFallbackResponse(botId, senderId, e.getMessage());
    }

    private Map<String, Object> buildCircuitBreakerFallbackResponse(String botId, String senderId, String error) {
        return Map.of(
            "status", "circuit_breaker",
            "response", "Xin chào! Hiện tại dịch vụ AI đang bận hoặc gặp sự cố kỹ thuật. Vui lòng thử lại sau vài phút hoặc liên hệ hỗ trợ nếu vấn đề kéo dài.",
            "botId", botId,
            "senderId", senderId,
            "error", error,
            "timestamp", System.currentTimeMillis()
        );
    }

    private Map<String, Object> buildFallbackResponse(String botId, String senderId) {
        return Map.of(
            "status", "fallback",
            "response", "Xin chào! Tôi là trợ lý AI (Claude). Đang trong quá trình cấu hình, vui lòng thử lại sau.",
            "botId", botId,
            "senderId", senderId,
            "timestamp", System.currentTimeMillis()
        );
    }

    private Map<String, Object> buildErrorResponse(String botId, String senderId, String error) {
        return Map.of(
            "status", "error",
            "response", "Rất tiếc, có lỗi xảy ra. Vui lòng thử lại sau ít phút.",
            "botId", botId,
            "senderId", senderId,
            "error", error,
            "timestamp", System.currentTimeMillis()
        );
    }
}
