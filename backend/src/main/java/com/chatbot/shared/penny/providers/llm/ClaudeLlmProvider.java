package com.chatbot.shared.penny.providers.llm;

import com.chatbot.shared.penny.model.LlmProviderType;
import com.chatbot.shared.penny.security.ApiKeyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Claude (Anthropic) LLM Provider implementation
 * Supports Claude 3.5 Sonnet, Claude 3 Haiku
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClaudeLlmProvider implements LlmProvider {

    private final ApiKeyManager apiKeyManager;
    private WebClient webClient;
    private String modelName = "claude-3-5-sonnet-20241022";
    private static final String API_URL = "https://api.anthropic.com/v1/messages";

    @Override
    public LlmResponse generateResponse(LlmRequest request) {
        String apiKey = apiKeyManager.getClaudeApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return LlmResponse.failure("Claude API key not configured");
        }

        long startTime = System.currentTimeMillis();
        try {
            // Build request body
            Map<String, Object> body = new HashMap<>();
            body.put("model", modelName);
            body.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 1000);
            
            if (request.getTemperature() != null) {
                body.put("temperature", request.getTemperature());
            }
            if (request.getTopP() != null) {
                body.put("top_p", request.getTopP());
            }

            // Build messages
            List<Map<String, String>> messages = new ArrayList<>();

            // Add system prompt
            if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
                body.put("system", request.getSystemPrompt());
            }

            // Add conversation history
            if (request.getConversationHistory() != null) {
                for (LlmRequest.Message msg : request.getConversationHistory()) {
                    Map<String, String> message = new HashMap<>();
                    message.put("role", msg.getRole());
                    message.put("content", msg.getContent());
                    messages.add(message);
                }
            }

            // Add current user message
            if (request.getUserMessage() != null) {
                Map<String, String> message = new HashMap<>();
                message.put("role", "user");
                message.put("content", request.getUserMessage());
                messages.add(message);
            }

            body.put("messages", messages);

            // Make API call
            Map<String, Object> response = getWebClient(apiKey)
                .post()
                .uri(API_URL)
                .headers(headers -> {
                    headers.set("x-api-key", apiKey);
                    headers.set("anthropic-version", "2023-06-01");
                    headers.set("Content-Type", "application/json");
                })
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            long processingTime = System.currentTimeMillis() - startTime;

            // Extract response
            if (response == null) {
                return LlmResponse.failure("Claude API returned null response");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> content = ((List<Map<String, Object>>) response.get("content")).get(0);
            String text = (String) content.get("text");

            @SuppressWarnings("unchecked")
            Map<String, Object> usage = (Map<String, Object>) response.get("usage");

            log.info("Claude response generated in {}ms, model: {}", processingTime, modelName);

            return LlmResponse.builder()
                .success(true)
                .text(text)
                .model(modelName)
                .promptTokens(usage != null ? ((Number) usage.get("input_tokens")).intValue() : null)
                .completionTokens(usage != null ? ((Number) usage.get("output_tokens")).intValue() : null)
                .totalTokens(usage != null ? ((Number) usage.get("input_tokens")).intValue() + 
                    ((Number) usage.get("output_tokens")).intValue() : null)
                .processingTimeMs(processingTime)
                .build();

        } catch (Exception e) {
            log.error("Claude API error: {}", e.getMessage(), e);
            return LlmResponse.failure("Claude API error: " + e.getMessage());
        }
    }

    @Override
    public boolean isHealthy() {
        String apiKey = apiKeyManager.getClaudeApiKey();
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public LlmProviderType getProviderType() {
        return LlmProviderType.CLAUDE;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    private WebClient getWebClient(String apiKey) {
        if (webClient == null) {
            webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        }
        return webClient;
    }
}
