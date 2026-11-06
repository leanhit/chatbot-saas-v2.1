package com.chatbot.core.ai.service;

import com.chatbot.core.ai.model.ConversationSentiment;
import com.chatbot.core.ai.model.SentimentLabel;
import com.chatbot.core.ai.model.SentimentSource;
import com.chatbot.core.ai.repository.ConversationSentimentRepository;
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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sentiment scoring service (Phase 5).
 *
 * Safety guarantees:
 * - Display/alert only — NEVER triggers automatic escalation or routing changes.
 * - Lexicon-based scoring is the deterministic fallback; AI scoring is attempted
 *   first when a provider is available and falls back to lexicon on any failure.
 * - All queries are tenant-scoped.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SentimentService {

    private static final int MAX_MESSAGES_TO_SCORE = 20;
    private static final Pattern SCORE_PATTERN = Pattern.compile("score[\"']?\\s*[:=]\\s*(-?[0-9]*\\.?[0-9]+)", Pattern.CASE_INSENSITIVE);

    private static final Set<String> POSITIVE_WORDS = Set.of(
        "good", "great", "excellent", "amazing", "wonderful", "happy", "thanks", "thank",
        "love", "perfect", "awesome", "nice", "helpful", "solved", "resolved", "appreciate",
        "tuyệt", "tốt", "cảm ơn", "hài lòng", "tuyệt vời", "tốt quá", "ok", "okay"
    );

    private static final Set<String> NEGATIVE_WORDS = Set.of(
        "bad", "terrible", "awful", "horrible", "angry", "upset", "disappointed", "frustrated",
        "hate", "worst", "broken", "fail", "failed", "error", "problem", "issue", "complaint",
        "tệ", "tồi", "dở", "chán", "bực", "tức", "phàn nàn", "lỗi", "sai", "không được"
    );

    private final ConversationSentimentRepository sentimentRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final PennyBotRepository pennyBotRepository;
    private final LlmRoutingService llmRoutingService;

    /**
     * Score recent user messages of a conversation and persist results.
     * Returns the latest sentiment score, or empty on failure.
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public Optional<ConversationSentiment> scoreConversation(Long conversationId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return Optional.empty();
        }
        try {
            if (conversationRepository.findByIdAndTenantId(conversationId, tenantId).isEmpty()) {
                return Optional.empty();
            }

            List<Message> messages = messageRepository.findByConversationIdAndTenantId(conversationId, tenantId);
            List<Message> userMessages = messages.stream()
                .filter(m -> "user".equalsIgnoreCase(m.getSender()) && m.getContent() != null)
                .toList();

            if (userMessages.isEmpty()) {
                return Optional.empty();
            }

            // Score the most recent batch of user messages
            int start = Math.max(0, userMessages.size() - MAX_MESSAGES_TO_SCORE);
            String combinedText = userMessages.subList(start, userMessages.size()).stream()
                .map(Message::getContent)
                .reduce((a, b) -> a + " " + b)
                .orElse("");

            ScoredText scored = scoreWithAiIfAvailable(tenantId, combinedText)
                .orElseGet(() -> scoreWithLexicon(combinedText));

            Message latest = userMessages.get(userMessages.size() - 1);
            ConversationSentiment sentiment = ConversationSentiment.builder()
                .conversationId(conversationId)
                .messageId(latest.getId())
                .score(scored.score())
                .label(scored.label())
                .source(scored.source())
                .build();
            sentiment.setTenantId(tenantId);

            return Optional.of(sentimentRepository.save(sentiment));
        } catch (Exception e) {
            log.error("[Sentiment] Failed to score conversation {}: {}", conversationId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true, transactionManager = "messageTransactionManager")
    public Optional<ConversationSentiment> getLatestSentiment(Long conversationId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return Optional.empty();
        }
        return sentimentRepository.findTopByTenantIdAndConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(
            tenantId, conversationId);
    }

    /**
     * Deterministic lexicon-based scoring. Exposed for tests and fallback use.
     */
    public ScoredText scoreWithLexicon(String text) {
        if (text == null || text.isBlank()) {
            return new ScoredText(0.0, SentimentLabel.NEUTRAL, SentimentSource.LEXICON);
        }
        String lower = text.toLowerCase(Locale.ROOT);
        int positive = 0;
        int negative = 0;
        for (String word : POSITIVE_WORDS) {
            if (lower.contains(word)) {
                positive++;
            }
        }
        for (String word : NEGATIVE_WORDS) {
            if (lower.contains(word)) {
                negative++;
            }
        }
        double score;
        if (positive == 0 && negative == 0) {
            score = 0.0;
        } else {
            score = (double) (positive - negative) / (positive + negative);
        }
        return new ScoredText(score, toLabel(score), SentimentSource.LEXICON);
    }

    private Optional<ScoredText> scoreWithAiIfAvailable(Long tenantId, String text) {
        try {
            PennyBot bot = pennyBotRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .stream().findFirst().orElse(null);
            if (bot == null) {
                return Optional.empty();
            }
            LlmRequest request = LlmRequest.builder()
                .systemPrompt("You are a sentiment analyzer. Score the sentiment of the customer messages " +
                              "on a scale from -1.0 (very negative) to 1.0 (very positive). " +
                              "Reply with ONLY: score: <number>")
                .userMessage(text)
                .temperature(0.0f)
                .maxTokens(20)
                .build();
            LlmResponse response = llmRoutingService.generateResponse(request, bot);
            if (response == null || !response.isSuccess() || response.getText() == null) {
                return Optional.empty();
            }
            Matcher m = SCORE_PATTERN.matcher(response.getText());
            if (!m.find()) {
                return Optional.empty();
            }
            double score = Double.parseDouble(m.group(1));
            score = Math.max(-1.0, Math.min(1.0, score));
            return Optional.of(new ScoredText(score, toLabel(score), SentimentSource.AI));
        } catch (Exception e) {
            log.warn("[Sentiment] AI scoring failed, falling back to lexicon: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private SentimentLabel toLabel(double score) {
        if (score > 0.2) {
            return SentimentLabel.POSITIVE;
        }
        if (score < -0.2) {
            return SentimentLabel.NEGATIVE;
        }
        return SentimentLabel.NEUTRAL;
    }

    public record ScoredText(double score, SentimentLabel label, SentimentSource source) {
    }
}
