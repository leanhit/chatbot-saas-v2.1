package com.chatbot.spokes.pennybot.service;

import com.chatbot.shared.penny.kb.KnowledgeBaseSearchService;
import com.chatbot.shared.penny.providers.PromptTemplateService;
import com.chatbot.spokes.facebook.webhook.service.ChatbotProviderService;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GPT Provider Service — Tích hợp OpenAI GPT-4o làm primary AI provider
 *
 * Được kích hoạt khi có OPENAI_API_KEY và penny.llm.openai.enabled=true.
 * Pipeline: System prompt (bot config + KB context) → OpenAI API → response
 */
@Service("gptProviderService")
@ConditionalOnProperty(name = "penny.llm.openai.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class GptProviderService implements ChatbotProviderService {

    @Value("${penny.llm.openai.api-key:}")
    private String apiKey;

    @Value("${penny.llm.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${penny.llm.openai.max-tokens:800}")
    private int maxTokens;

    @Value("${penny.llm.openai.temperature:0.7}")
    private double temperature;

    @Value("${penny.llm.openai.timeout-seconds:30}")
    private int timeoutSeconds;

    private final PromptTemplateService promptTemplateService;
    private final KnowledgeBaseSearchService knowledgeBaseSearchService;

    private OpenAIClient openAIClient;

    public GptProviderService(PromptTemplateService promptTemplateService,
                              KnowledgeBaseSearchService knowledgeBaseSearchService) {
        this.promptTemplateService = promptTemplateService;
        this.knowledgeBaseSearchService = knowledgeBaseSearchService;
    }

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("⚠️ GPT Provider: OPENAI_API_KEY is not set. Provider will return fallback responses.");
            return;
        }
        this.openAIClient = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
        log.info("✅ GPT Provider initialized with model: {}", model);
    }

    @Override
    public Map<String, Object> sendMessage(String botId, String senderId, String messageText) {
        log.info("🤖 [GPT] Processing message from {} via bot {}: {}",
            senderId, botId, messageText.length() > 50 ? messageText.substring(0, 50) + "..." : messageText);

        if (openAIClient == null) {
            return buildFallbackResponse(botId, senderId, "GPT provider chưa được cấu hình API key.");
        }

        try {
            UUID botUuid = parseUUID(botId);

            // Retrieve knowledge context from RAG search (if enabled)
            List<String> knowledgeSnippets = List.of();
            if (knowledgeBaseSearchService != null && knowledgeBaseSearchService.isEnabled()) {
                try {
                    String knowledgeContext = knowledgeBaseSearchService.searchAndFormatContext(
                        botUuid, null, messageText); // tenantId null for now - will need to pass from context
                    if (knowledgeContext != null && !knowledgeContext.isBlank()) {
                        knowledgeSnippets = List.of(knowledgeContext);
                        log.debug("📚 Retrieved KB context ({} chars) for RAG", knowledgeContext.length());
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Failed to retrieve KB context: {}", e.getMessage());
                }
            }

            // Build system prompt with bot config + KB context
            String systemPrompt = promptTemplateService.buildSystemPrompt(
                botUuid, knowledgeSnippets, List.of()); // No conversation history for now

            // Build messages list
            List<ChatCompletionMessageParam> messages = new ArrayList<>();
            messages.add(ChatCompletionMessageParam.ofSystem(
                ChatCompletionSystemMessageParam.builder()
                    .content(systemPrompt)
                    .build()));
            messages.add(ChatCompletionMessageParam.ofUser(
                ChatCompletionUserMessageParam.builder()
                    .content(messageText)
                    .build()));

            // Call OpenAI API
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(model)
                .maxCompletionTokens(maxTokens)
                .temperature(temperature)
                .messages(messages)
                .build();

            ChatCompletion completion = openAIClient.chat().completions().create(params);

            String responseText = completion.choices().stream()
                .findFirst()
                .flatMap(choice -> choice.message().content())
                .orElse("Xin lỗi, tôi không thể tạo phản hồi lúc này. Vui lòng thử lại.");

            log.info("✅ [GPT] Response generated ({} chars), model: {}, tokens used: {}",
                responseText.length(), model,
                completion.usage().map(u -> u.totalTokens()).orElse(0L));

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("response", responseText);
            response.put("botId", botId);
            response.put("senderId", senderId);
            response.put("provider", "GPT");
            response.put("model", model);
            response.put("tokensUsed", completion.usage().map(u -> u.totalTokens()).orElse(0L));
            response.put("timestamp", System.currentTimeMillis());
            return response;

        } catch (Exception e) {
            log.error("❌ [GPT] Error calling OpenAI API: {}", e.getMessage(), e);
            return buildErrorResponse(botId, senderId, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> sendEvent(String botId, String senderId,
                                          String eventName, Map<String, Object> payload) {
        log.info("📡 [GPT] Event received: {} for bot: {}", eventName, botId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Event acknowledged by GPT provider");
        response.put("eventName", eventName);
        response.put("processedAt", System.currentTimeMillis());
        return response;
    }

    @Override
    public boolean healthCheck(String botId) {
        if (openAIClient == null) {
            log.warn("⚠️ [GPT] Health check failed: client not initialized");
            return false;
        }
        try {
            // Ping bằng cách list models — nhẹ và nhanh
            openAIClient.models().list();
            log.debug("✅ [GPT] Health check passed");
            return true;
        } catch (Exception e) {
            log.error("❌ [GPT] Health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProviderType() {
        return "GPT";
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    private UUID parseUUID(String botId) {
        try {
            return UUID.fromString(botId);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ [GPT] Invalid botId UUID: {}, using zero UUID", botId);
            return new UUID(0, 0);
        }
    }

    private Map<String, Object> buildFallbackResponse(String botId, String senderId, String reason) {
        Map<String, Object> r = new HashMap<>();
        r.put("status", "fallback");
        r.put("response", "Xin chào! Tôi là trợ lý AI. Hiện tại đang trong quá trình cấu hình, vui lòng thử lại sau.");
        r.put("botId", botId);
        r.put("senderId", senderId);
        r.put("reason", reason);
        r.put("timestamp", System.currentTimeMillis());
        return r;
    }

    private Map<String, Object> buildErrorResponse(String botId, String senderId, String error) {
        Map<String, Object> r = new HashMap<>();
        r.put("status", "error");
        r.put("response", "Rất tiếc, có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại sau ít phút.");
        r.put("botId", botId);
        r.put("senderId", senderId);
        r.put("error", error);
        r.put("timestamp", System.currentTimeMillis());
        return r;
    }
}
