package com.chatbot.shared.penny.providers.llm;

import com.chatbot.shared.penny.model.LlmProviderType;
import com.chatbot.shared.penny.security.ApiKeyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google Gemini LLM Provider implementation
 * Supports Gemini 1.5 Pro, Gemini 1.5 Flash
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiLlmProvider implements LlmProvider {

    private final ApiKeyManager apiKeyManager;
    private WebClient webClient;
    private String modelName = "gemini-1.5-pro";
    private static final String API_BASE = "https://generativelanguage.googleapis.com/v1beta/models";

    @Override
    public LlmResponse generateResponse(LlmRequest request) {
        String apiKey = apiKeyManager.getGeminiApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return LlmResponse.failure("Gemini API key not configured");
        }

        long startTime = System.currentTimeMillis();
        try {
            // Build request body
            Map<String, Object> body = new HashMap<>();
            
            // Build contents
            List<Map<String, Object>> contents = new ArrayList<>();

            // Add system prompt and user message
            StringBuilder fullPrompt = new StringBuilder();
            if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
                fullPrompt.append("System: ").append(request.getSystemPrompt()).append("\n\n");
            }

            // Add conversation history
            if (request.getConversationHistory() != null) {
                for (LlmRequest.Message msg : request.getConversationHistory()) {
                    fullPrompt.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
                }
            }

            // Add current user message
            if (request.getUserMessage() != null) {
                fullPrompt.append("User: ").append(request.getUserMessage());
            }

            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", fullPrompt.toString());
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);

            body.put("contents", contents);

            // Add generation config
            Map<String, Object> generationConfig = new HashMap<>();
            if (request.getTemperature() != null) {
                generationConfig.put("temperature", request.getTemperature());
            }
            if (request.getMaxTokens() != null) {
                generationConfig.put("maxOutputTokens", request.getMaxTokens());
            }
            if (request.getTopP() != null) {
                generationConfig.put("topP", request.getTopP());
            }
            body.put("generationConfig", generationConfig);

            // Make API call
            String url = String.format("%s/%s:generateContent?key=%s", API_BASE, modelName, apiKey);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = getWebClient()
                .post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            long processingTime = System.currentTimeMillis() - startTime;

            // Extract response
            if (response == null) {
                return LlmResponse.failure("Gemini API returned null response");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return LlmResponse.failure("Gemini API returned no candidates");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> contentResponse = (Map<String, Object>) candidates.get(0).get("content");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> partsResponse = (List<Map<String, String>>) contentResponse.get("parts");
            String text = partsResponse.get(0).get("text");

            @SuppressWarnings("unchecked")
            Map<String, Object> usageMetadata = (Map<String, Object>) response.get("usageMetadata");

            log.info("Gemini response generated in {}ms, model: {}", processingTime, modelName);

            return LlmResponse.builder()
                .success(true)
                .text(text)
                .model(modelName)
                .promptTokens(usageMetadata != null ? 
                    ((Number) usageMetadata.get("promptTokenCount")).intValue() : null)
                .completionTokens(usageMetadata != null ? 
                    ((Number) usageMetadata.get("candidatesTokenCount")).intValue() : null)
                .totalTokens(usageMetadata != null ? 
                    ((Number) usageMetadata.get("totalTokenCount")).intValue() : null)
                .processingTimeMs(processingTime)
                .build();

        } catch (Exception e) {
            log.error("Gemini API error: {}", e.getMessage(), e);
            return LlmResponse.failure("Gemini API error: " + e.getMessage());
        }
    }

    @Override
    public boolean isHealthy() {
        String apiKey = apiKeyManager.getGeminiApiKey();
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public LlmProviderType getProviderType() {
        return LlmProviderType.GEMINI;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        }
        return webClient;
    }
}
