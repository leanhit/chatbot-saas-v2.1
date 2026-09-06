package com.chatbot.shared.penny.providers.llm;

import com.chatbot.shared.penny.model.LlmProviderType;
import com.chatbot.shared.penny.security.ApiKeyManager;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI LLM Provider implementation
 * Supports GPT-4o, GPT-4o-mini, GPT-3.5-turbo
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiLlmProvider implements LlmProvider {

    private final ApiKeyManager apiKeyManager;
    private OpenAIClient client;
    private String modelName = "gpt-4o-mini";
    private boolean initialized = false;

    @Override
    public LlmResponse generateResponse(LlmRequest request) {
        if (!initializeClient()) {
            return LlmResponse.failure("OpenAI client initialization failed - API key not configured");
        }

        long startTime = System.currentTimeMillis();
        try {
            // Build messages
            List<ChatCompletionMessageParam> messages = new ArrayList<>();

            // Add system prompt if provided
            if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
                messages.add(ChatCompletionMessageParam.ofSystem(
                    ChatCompletionSystemMessageParam.builder()
                        .content(request.getSystemPrompt())
                        .build()));
            }

            // Add conversation history if provided
            if (request.getConversationHistory() != null) {
                for (LlmRequest.Message msg : request.getConversationHistory()) {
                    switch (msg.getRole().toLowerCase()) {
                        case "system" -> messages.add(ChatCompletionMessageParam.ofSystem(
                            ChatCompletionSystemMessageParam.builder().content(msg.getContent()).build()));
                        case "user" -> messages.add(ChatCompletionMessageParam.ofUser(
                            ChatCompletionUserMessageParam.builder().content(msg.getContent()).build()));
                        case "assistant" -> messages.add(ChatCompletionMessageParam.ofAssistant(
                            ChatCompletionAssistantMessageParam.builder().content(msg.getContent()).build()));
                    }
                }
            }

            // Add current user message
            if (request.getUserMessage() != null) {
                messages.add(ChatCompletionMessageParam.ofUser(
                    ChatCompletionUserMessageParam.builder()
                        .content(request.getUserMessage())
                        .build()));
            }

            // Build completion params
            ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                .model(modelName)
                .messages(messages);

            if (request.getTemperature() != null) {
                paramsBuilder.temperature(request.getTemperature().doubleValue());
            }
            if (request.getMaxTokens() != null) {
                paramsBuilder.maxCompletionTokens(request.getMaxTokens().longValue());
            }

            ChatCompletion completion = client.chat().completions().create(paramsBuilder.build());

            // Extract response
            String text = completion.choices().stream()
                .findFirst()
                .flatMap(choice -> choice.message().content())
                .orElse("");

            long processingTime = System.currentTimeMillis() - startTime;
            long promptTokens = completion.usage().map(u -> u.promptTokens()).orElse(0L);
            long completionTokens = completion.usage().map(u -> u.completionTokens()).orElse(0L);
            long totalTokens = completion.usage().map(u -> u.totalTokens()).orElse(0L);

            log.info("OpenAI response generated in {}ms, model: {}", processingTime, modelName);

            return LlmResponse.builder()
                .success(true)
                .text(text)
                .model(modelName)
                .promptTokens((int) promptTokens)
                .completionTokens((int) completionTokens)
                .totalTokens((int) totalTokens)
                .finishReason("stop")
                .processingTimeMs(processingTime)
                .build();

        } catch (Exception e) {
            log.error("OpenAI API error: {}", e.getMessage(), e);
            return LlmResponse.failure("OpenAI API error: " + e.getMessage());
        }
    }

    @Override
    public boolean isHealthy() {
        return initialized && client != null;
    }

    @Override
    public LlmProviderType getProviderType() {
        return LlmProviderType.OPENAI;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    private boolean initializeClient() {
        if (initialized) {
            return true;
        }

        String apiKey = apiKeyManager.getOpenAiApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OpenAI API key not configured");
            return false;
        }

        try {
            this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
            this.initialized = true;
            log.info("OpenAI client initialized with model: {}", modelName);
            return true;
        } catch (Exception e) {
            log.error("Failed to initialize OpenAI client: {}", e.getMessage(), e);
            return false;
        }
    }
}
