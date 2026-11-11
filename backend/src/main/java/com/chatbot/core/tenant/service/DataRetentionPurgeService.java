package com.chatbot.core.tenant.service;

import com.chatbot.core.ai.repository.AiSuggestionRepository;
import com.chatbot.core.ai.repository.ConversationSentimentRepository;
import com.chatbot.core.customer.repository.FeedbackRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Data retention purge job (Phase 6).
 *
 * Hard-deletes records that have been soft-deleted (is_deleted = true)
 * for longer than the configured retention period. Runs daily; each
 * entity type is purged independently so one failure does not block
 * the others. Retention periods are configurable via
 * {@code app.retention.purge.*-days} properties.
 *
 * Note: messages are additionally archived to messages_archive by
 * MessageArchivingService before this purge applies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataRetentionPurgeService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final FeedbackRepository feedbackRepository;
    private final AiSuggestionRepository aiSuggestionRepository;
    private final ConversationSentimentRepository conversationSentimentRepository;

    @Value("${app.retention.purge.conversations-days:365}")
    private int conversationsRetentionDays;

    @Value("${app.retention.purge.messages-days:365}")
    private int messagesRetentionDays;

    @Value("${app.retention.purge.feedback-days:730}")
    private int feedbackRetentionDays;

    @Value("${app.retention.purge.ai-data-days:365}")
    private int aiDataRetentionDays;

    /**
     * Daily purge of soft-deleted records past their retention TTL.
     * Runs at 04:30, after message archiving (03:00) and tenant cleanup (02:00).
     */
    @Scheduled(cron = "0 30 4 * * ?")
    @SchedulerLock(name = "DataRetentionPurgeService_purge", lockAtMostFor = "1h", lockAtLeastFor = "30m")
    public void purgeSoftDeletedRecords() {
        log.info("[DataRetention] Starting soft-delete purge job");

        purge("conversations", conversationsRetentionDays,
                () -> conversationRepository.purgeSoftDeletedBefore(cutoff(conversationsRetentionDays)));
        purge("messages", messagesRetentionDays,
                () -> messageRepository.purgeSoftDeletedBefore(cutoff(messagesRetentionDays)));
        purge("feedback", feedbackRetentionDays,
                () -> feedbackRepository.purgeSoftDeletedBefore(cutoff(feedbackRetentionDays)));
        purge("ai_suggestions", aiDataRetentionDays,
                () -> aiSuggestionRepository.purgeSoftDeletedBefore(cutoff(aiDataRetentionDays)));
        purge("conversation_sentiment", aiDataRetentionDays,
                () -> conversationSentimentRepository.purgeSoftDeletedBefore(cutoff(aiDataRetentionDays)));

        log.info("[DataRetention] Purge job completed");
    }

    private LocalDateTime cutoff(int retentionDays) {
        return LocalDateTime.now().minusDays(retentionDays);
    }

    private void purge(String entityName, int retentionDays, java.util.function.IntSupplier action) {
        try {
            int deleted = action.getAsInt();
            if (deleted > 0) {
                log.info("[DataRetention] Purged {} {} records soft-deleted > {} days ago",
                        deleted, entityName, retentionDays);
            }
        } catch (Exception e) {
            log.error("[DataRetention] Failed to purge {}: {}", entityName, e.getMessage(), e);
        }
    }
}
