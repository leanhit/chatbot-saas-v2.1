package com.chatbot.core.message.store.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for interacting with LLM APIs (OpenAI, Anthropic, etc.)
 * Implements Phase 3.3: AI-based Escalation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LLMClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${llm.provider:openai}")
    private String llmProvider;

    @Value("${llm.api.key:}")
    private String apiKey;

    @Value("${llm.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${llm.model:gpt-3.5-turbo}")
    private String model;

    @Value("${llm.enabled:false}")
    private Boolean enabled;

    /**
     * Send prompt to LLM and get response
     */
    public String sendPrompt(String systemPrompt, String userPrompt) {
        if (!enabled || apiKey == null || apiKey.isEmpty()) {
            log.warn("LLM client is disabled or API key not configured");
            return null;
        }

        try {
            Map<String, Object> requestBody = buildRequestBody(systemPrompt, userPrompt);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractContentFromResponse(response.getBody());
            } else {
                log.error("LLM API returned error: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error calling LLM API", e);
            return null;
        }
    }

    /**
     * Build request body for LLM API
     */
    private Map<String, Object> buildRequestBody(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 500);

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);

        requestBody.put("messages", List.of(systemMessage, userMessage));

        return requestBody;
    }

    /**
     * Extract content from LLM response
     */
    private String extractContentFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message");
                return message.path("content").asText();
            }
        } catch (Exception e) {
            log.error("Error parsing LLM response", e);
        }
        return null;
    }

    /**
     * Analyze sentiment of text
     * Returns: "positive", "negative", "neutral"
     */
    public String analyzeSentiment(String text) {
        String systemPrompt = "You are a sentiment analyzer. Analyze the sentiment of the given text and respond with ONLY one word: 'positive', 'negative', or 'neutral'.";
        String userPrompt = "Analyze the sentiment of this text: " + text;

        String response = sendPrompt(systemPrompt, userPrompt);
        if (response != null) {
            String sentiment = response.toLowerCase().trim();
            if (sentiment.contains("positive")) return "positive";
            if (sentiment.contains("negative")) return "negative";
            return "neutral";
        }
        return "neutral"; // Default fallback
    }

    /**
     * Detect complexity of conversation
     * Returns: "low", "medium", "high"
     */
    public String detectComplexity(String conversationText) {
        String systemPrompt = "You are a conversation complexity analyzer. Analyze the complexity of the given conversation and respond with ONLY one word: 'low', 'medium', or 'high'. Consider factors like technical terms, emotional intensity, and problem complexity.";
        String userPrompt = "Analyze the complexity of this conversation: " + conversationText;

        String response = sendPrompt(systemPrompt, userPrompt);
        if (response != null) {
            String complexity = response.toLowerCase().trim();
            if (complexity.contains("high")) return "high";
            if (complexity.contains("medium")) return "medium";
            return "low";
        }
        return "medium"; // Default fallback
    }

    /**
     * Determine if escalation is needed
     * Returns: true if escalation is recommended, false otherwise
     */
    public boolean shouldEscalate(String conversationText, String customerTier, String sentiment) {
        String systemPrompt = "You are an escalation decision engine. Analyze if the conversation should be escalated to a human agent. Respond with ONLY 'yes' or 'no'.";
        String userPrompt = String.format(
            "Should this conversation be escalated? Customer tier: %s, Sentiment: %s, Conversation: %s",
            customerTier, sentiment, conversationText
        );

        String response = sendPrompt(systemPrompt, userPrompt);
        if (response != null) {
            return response.toLowerCase().trim().contains("yes");
        }
        return false; // Default fallback
    }

    /**
     * Generate escalation summary
     */
    public String generateEscalationSummary(String conversationText) {
        String systemPrompt = "You are a conversation summarizer. Generate a concise summary of the conversation in 2-3 sentences.";
        String userPrompt = "Summarize this conversation: " + conversationText;

        return sendPrompt(systemPrompt, userPrompt);
    }

    /**
     * Check if LLM client is enabled
     */
    public boolean isEnabled() {
        return enabled && apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Get LLM provider
     */
    public String getProvider() {
        return llmProvider;
    }
}
