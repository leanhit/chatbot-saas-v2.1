package com.chatbot.shared.penny.providers.llm;

import com.chatbot.shared.penny.model.LlmProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ollama LLM Provider implementation for local/on-premise models
 * Supports Llama 3, Mistral, and other Ollama-compatible models
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaLlmProvider implements LlmProvider {

    @Value("${penny.llm.ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    private WebClient webClient;
    private String modelName = "llama3:8b";

    @Override
    public LlmResponse generateResponse(LlmRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            // Build request body
            Map<String, Object> body = new HashMap<>();
            body.put("model", modelName);
            body.put("stream", false);

            // Build prompt
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

            body.put("prompt", fullPrompt.toString());

            // Add options
            Map<String, Object> options = new HashMap<>();
            if (request.getTemperature() != null) {
                options.put("temperature", request.getTemperature());
            }
            if (request.getTopP() != null) {
                options.put("top_p", request.getTopP());
            }
            if (request.getMaxTokens() != null) {
                options.put("num_predict", request.getMaxTokens());
            }
            if (!options.isEmpty()) {
                body.put("options", options);
            }

            // Make API call
            String url = String.format("%s/api/generate", baseUrl);

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
                return LlmResponse.failure("Ollama API returned null response");
            }

            String text = (String) response.get("response");
            if (text == null) {
                return LlmResponse.failure("Ollama API returned no response text");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> promptEvalCount = (Map<String, Object>) response.get("prompt_eval_count");
            @SuppressWarnings("unchecked")
            Map<String, Object> evalCount = (Map<String, Object>) response.get("eval_count");

            log.info("Ollama response generated in {}ms, model: {}", processingTime, modelName);

            return LlmResponse.builder()
                .success(true)
                .text(text)
                .model(modelName)
                .promptTokens(promptEvalCount != null ? ((Number) promptEvalCount).intValue() : null)
                .completionTokens(evalCount != null ? ((Number) evalCount).intValue() : null)
                .totalTokens(promptEvalCount != null && evalCount != null ? 
                    ((Number) promptEvalCount).intValue() + ((Number) evalCount).intValue() : null)
                .processingTimeMs(processingTime)
                .build();

        } catch (Exception e) {
            log.error("Ollama API error: {}", e.getMessage(), e);
            return LlmResponse.failure("Ollama API error: " + e.getMessage());
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            String url = String.format("%s/api/tags", baseUrl);
            getWebClient()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .block();
            return true;
        } catch (Exception e) {
            log.warn("Ollama health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public LlmProviderType getProviderType() {
        return LlmProviderType.OLLAMA;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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
