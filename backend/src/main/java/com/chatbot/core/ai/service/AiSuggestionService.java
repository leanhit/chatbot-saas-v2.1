package com.chatbot.core.ai.service;

import com.chatbot.core.ai.model.AiSuggestion;
import com.chatbot.core.ai.model.AiSuggestionStatus;
import com.chatbot.core.ai.repository.AiSuggestionRepository;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.penny.model.PennyBot;
import com.chatbot.core.penny.providers.llm.LlmRequest;
import com.chatbot.core.penny.providers.llm.LlmResponse;
import com.chatbot.core.penny.providers.llm.LlmRoutingService;
import com.chatbot.core.penny.repository.PennyBotRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI suggestion service for agent assistance.
 *
 * Safety guarantees (Phase 5):
 * - Suggestions are display-only; never auto-applied to conversations.
 * - AI provider failure never breaks the conversation flow — errors are
 *   logged and surfaced as a failed result, not thrown to callers.
 * - All queries are tenant-scoped.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiSuggestionService {

    private static final int MAX_CONTEXT_MESSAGES = 10;
    private static final Pattern CONFIDENCE_PATTERN = Pattern.compile("confidence[\"']?\\s*[:=]\\s*([0-9]*\\.?[0-9]+)", Pattern.CASE_INSENSITIVE);

    private final AiSuggestionRepository suggestionRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final PennyBotRepository pennyBotRepository;
    private final LlmRoutingService llmRoutingService;

    /**
     * Generate an AI suggestion for a conversation.
     * Returns empty Optional on any failure (never throws to caller).
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public Optional<AiSuggestion> generateSuggestion(Long conversationId, Long agentId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.warn("[AI Suggestion] No tenant context, skipping generation for conversation {}", conversationId);
            return Optional.empty();
        }

        try {
            Conversation conversation = conversationRepository.findByIdAndTenantId(conversationId, tenantId)
                .orElse(null);
            if (conversation == null) {
                log.warn("[AI Suggestion] Conversation {} not found for tenant {}", conversationId, tenantId);
                return Optional.empty();
            }

            PennyBot bot = pennyBotRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .stream().findFirst().orElse(null);
            if (bot == null) {
                log.warn("[AI Suggestion] No active PennyBot for tenant {}, cannot generate suggestion", tenantId);
                return Optional.empty();
            }

            List<Message> recentMessages = messageRepository.findByConversationIdAndTenantId(conversationId, tenantId);
            List<LlmRequest.Message> history = buildHistory(recentMessages);

            LlmRequest request = LlmRequest.builder()
                .systemPrompt(buildSystemPrompt())
                .userMessage(buildUserMessage(conversation, recentMessages))
                .conversationHistory(history)
                .temperature(0.4f)
                .maxTokens(300)
                .build();

            LlmResponse response = llmRoutingService.generateResponse(request, bot);
            if (response == null || !response.isSuccess() || response.getText() == null || response.getText().isBlank()) {
                log.warn("[AI Suggestion] LLM provider failed for conversation {}: {}",
                    conversationId, response != null ? response.getErrorMessage() : "null response");
                return Optional.empty();
            }

            String rawText = response.getText().trim();
            Double confidence = extractConfidence(rawText);
            String suggestionText = stripConfidenceMarker(rawText);

            AiSuggestion suggestion = AiSuggestion.builder()
                .conversationId(conversationId)
                .agentId(agentId)
                .suggestionText(suggestionText)
                .confidence(confidence)
                .provider(bot.getProviderType() != null ? bot.getProviderType().name() : null)
                .model(bot.getModelName())
                .status(AiSuggestionStatus.PENDING)
                .build();
            suggestion.setTenantId(tenantId);

            AiSuggestion saved = suggestionRepository.save(suggestion);
            log.info("[AI Suggestion] Generated suggestion {} for conversation {} (confidence={})",
                saved.getId(), conversationId, confidence);
            return Optional.of(saved);
        } catch (Exception e) {
            log.error("[AI Suggestion] Failed to generate suggestion for conversation {}: {}",
                conversationId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true, transactionManager = "messageTransactionManager")
    public List<AiSuggestion> getSuggestionsForConversation(Long conversationId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }
        return suggestionRepository.findByTenantIdAndConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(
            tenantId, conversationId);
    }

    @Transactional(readOnly = true, transactionManager = "messageTransactionManager")
    public List<AiSuggestion> getPendingSuggestions() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return List.of();
        }
        return suggestionRepository.findByTenantIdAndStatusAndIsDeletedFalseOrderByCreatedAtDesc(
            tenantId, AiSuggestionStatus.PENDING);
    }

    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public Optional<AiSuggestion> acceptSuggestion(Long suggestionId) {
        return respondToSuggestion(suggestionId, AiSuggestionStatus.ACCEPTED, null);
    }

    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public Optional<AiSuggestion> rejectSuggestion(Long suggestionId, String note) {
        return respondToSuggestion(suggestionId, AiSuggestionStatus.REJECTED, note);
    }

    /**
     * Mark stale PENDING suggestions as EXPIRED (e.g. scheduled cleanup).
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public int expireStaleSuggestions(Long tenantId, int olderThanHours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(olderThanHours);
        List<AiSuggestion> stale = suggestionRepository.findPendingOlderThan(
            tenantId, AiSuggestionStatus.PENDING, cutoff);
        for (AiSuggestion s : stale) {
            s.setStatus(AiSuggestionStatus.EXPIRED);
            s.setRespondedAt(LocalDateTime.now());
        }
        suggestionRepository.saveAll(stale);
        return stale.size();
    }

    private Optional<AiSuggestion> respondToSuggestion(Long suggestionId, AiSuggestionStatus status, String note) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return Optional.empty();
        }
        Optional<AiSuggestion> opt = suggestionRepository.findByIdAndTenantIdAndIsDeletedFalse(suggestionId, tenantId);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        AiSuggestion suggestion = opt.get();
        if (suggestion.getStatus() != AiSuggestionStatus.PENDING) {
            log.warn("[AI Suggestion] Suggestion {} already in status {}, cannot transition to {}",
                suggestionId, suggestion.getStatus(), status);
            return Optional.empty();
        }
        suggestion.setStatus(status);
        suggestion.setResponseNote(note);
        suggestion.setRespondedAt(LocalDateTime.now());
        return Optional.of(suggestionRepository.save(suggestion));
    }

    private String buildSystemPrompt() {
        return "You are an AI assistant helping a human customer support agent. " +
               "Based on the conversation context, suggest a helpful, concise reply the agent could send to the customer. " +
               "Do NOT send anything yourself — this is only a suggestion for the agent to review. " +
               "Reply in the same language as the customer. " +
               "End your response with a line: confidence: <number between 0 and 1>.";
    }

    private String buildUserMessage(Conversation conversation, List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("Customer: ").append(
            conversation.getUserName() != null ? conversation.getUserName() : conversation.getExternalUserId());
        sb.append("\nRecent conversation:\n");
        int start = Math.max(0, messages.size() - MAX_CONTEXT_MESSAGES);
        for (int i = start; i < messages.size(); i++) {
            Message m = messages.get(i);
            sb.append(m.getSender()).append(": ").append(m.getContent()).append("\n");
        }
        sb.append("\nSuggest the next agent reply:");
        return sb.toString();
    }

    private List<LlmRequest.Message> buildHistory(List<Message> messages) {
        List<LlmRequest.Message> history = new ArrayList<>();
        int start = Math.max(0, messages.size() - MAX_CONTEXT_MESSAGES);
        for (int i = start; i < messages.size(); i++) {
            Message m = messages.get(i);
            String role = "agent".equalsIgnoreCase(m.getSender()) ? "assistant" : "user";
            history.add(LlmRequest.Message.builder()
                .role(role)
                .content(m.getContent())
                .build());
        }
        return history;
    }

    private Double extractConfidence(String text) {
        Matcher m = CONFIDENCE_PATTERN.matcher(text);
        if (m.find()) {
            try {
                double v = Double.parseDouble(m.group(1));
                if (v >= 0 && v <= 1) {
                    return v;
                }
                if (v > 1 && v <= 100) {
                    return v / 100.0;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String stripConfidenceMarker(String text) {
        return text.replaceAll("(?im)^\\s*confidence\\s*[:=]\\s*[0-9]*\\.?[0-9]+\\s*$", "").trim();
    }
}
