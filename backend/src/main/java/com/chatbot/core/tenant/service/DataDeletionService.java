package com.chatbot.core.tenant.service;

import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.customer.repository.FeedbackRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.tenant.model.DataDeletionRequest;
import com.chatbot.core.tenant.repository.DataDeletionRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GDPR data deletion workflow (Phase 6).
 *
 * Flow: request (PENDING, scheduled +30 days grace) → process
 * (anonymize customer PII, delete messages/conversations/feedback)
 * → COMPLETED. Can be triggered manually by admin or by the
 * scheduled job once scheduledAt passes.
 *
 * Note: deletion spans multiple module databases, so it is
 * executed as sequential steps rather than a single transaction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataDeletionService {

    private static final int GRACE_PERIOD_DAYS = 30;

    private final DataDeletionRequestRepository requestRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final FeedbackRepository feedbackRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final TenantAuditLogService auditLogService;

    @Transactional(value = "tenantTransactionManager", rollbackFor = Exception.class)
    public DataDeletionRequest createRequest(Long tenantId, String tenantKey, String requestedBy, String reason) {
        DataDeletionRequest request = DataDeletionRequest.builder()
            .tenantId(tenantId)
            .tenantKey(tenantKey)
            .requestedBy(requestedBy)
            .reason(reason)
            .status(DataDeletionRequest.Status.PENDING)
            .scheduledAt(LocalDateTime.now().plusDays(GRACE_PERIOD_DAYS))
            .build();
        DataDeletionRequest saved = requestRepository.save(request);
        auditLogService.logAction(tenantId, requestedBy, "GDPR_DELETION_REQUESTED",
            "Deletion request id=" + saved.getId() + " scheduled=" + saved.getScheduledAt());
        return saved;
    }

    @Transactional(value = "tenantTransactionManager", readOnly = true)
    public List<DataDeletionRequest> listRequests(Long tenantId) {
        return requestRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Transactional(value = "tenantTransactionManager", rollbackFor = Exception.class)
    public DataDeletionRequest cancelRequest(Long requestId, Long tenantId, String cancelledBy) {
        DataDeletionRequest request = requestRepository.findByIdAndTenantId(requestId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Deletion request not found: " + requestId));
        if (request.getStatus() != DataDeletionRequest.Status.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be cancelled");
        }
        request.setStatus(DataDeletionRequest.Status.CANCELLED);
        request.setProcessedBy(cancelledBy);
        request.setProcessedAt(LocalDateTime.now());
        auditLogService.logAction(tenantId, cancelledBy, "GDPR_DELETION_CANCELLED",
            "Deletion request id=" + requestId + " cancelled");
        return requestRepository.save(request);
    }

    /**
     * Process a deletion request: anonymize customer PII and delete
     * messages, conversations, feedback for the tenant.
     */
    public DataDeletionRequest processRequest(Long requestId, String processedBy) {
        DataDeletionRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Deletion request not found: " + requestId));
        if (request.getStatus() == DataDeletionRequest.Status.COMPLETED
            || request.getStatus() == DataDeletionRequest.Status.CANCELLED) {
            throw new IllegalStateException("Request already " + request.getStatus());
        }

        Long tenantId = request.getTenantId();
        request.setStatus(DataDeletionRequest.Status.PROCESSING);
        requestRepository.save(request);

        StringBuilder summary = new StringBuilder();
        try {
            // 1. Anonymize customer profiles (app DB)
            int profiles = anonymizeCustomerProfiles(tenantId);
            summary.append("anonymized_profiles=").append(profiles).append("; ");

            // 2. Delete feedback (app DB)
            int feedback = deleteFeedback(tenantId);
            summary.append("deleted_feedback=").append(feedback).append("; ");

            // 3. Delete messages then conversations (message DB)
            int messages = deleteMessages(tenantId);
            summary.append("deleted_messages=").append(messages).append("; ");
            int conversations = deleteConversations(tenantId);
            summary.append("deleted_conversations=").append(conversations);

            request.setStatus(DataDeletionRequest.Status.COMPLETED);
            request.setResultSummary(summary.toString());
            log.info("[GDPR] Deletion completed for tenant {}: {}", tenantId, summary);
        } catch (Exception e) {
            request.setStatus(DataDeletionRequest.Status.FAILED);
            request.setResultSummary("FAILED: " + e.getMessage() + " | partial: " + summary);
            log.error("[GDPR] Deletion failed for tenant {}: {}", tenantId, e.getMessage(), e);
        }

        request.setProcessedAt(LocalDateTime.now());
        request.setProcessedBy(processedBy);
        DataDeletionRequest saved = requestRepository.save(request);
        auditLogService.logAction(tenantId, processedBy, "GDPR_DELETION_" + saved.getStatus().name(),
            "Deletion request id=" + requestId + " " + saved.getResultSummary());
        return saved;
    }

    /** Scheduled job: auto-process PENDING requests past their scheduledAt. */
    @Scheduled(cron = "0 0 * * * *")
    public void processDueRequests() {
        List<DataDeletionRequest> due = requestRepository
            .findByStatusAndScheduledAtBefore(DataDeletionRequest.Status.PENDING, LocalDateTime.now());
        for (DataDeletionRequest r : due) {
            try {
                log.info("[GDPR] Auto-processing due deletion request {} for tenant {}", r.getId(), r.getTenantId());
                processRequest(r.getId(), "system");
            } catch (Exception e) {
                log.error("[GDPR] Failed to auto-process request {}: {}", r.getId(), e.getMessage());
            }
        }
    }

    // ─── Step implementations ───
    // Each repository method carries its own @Transactional with the
    // correct transaction manager; calling them directly avoids
    // self-invocation proxy bypass.

    protected int anonymizeCustomerProfiles(Long tenantId) {
        List<CustomerProfile> profiles = customerProfileRepository.findByTenantId(tenantId);
        int count = 0;
        for (CustomerProfile p : profiles) {
            p.setName("[deleted]");
            p.setEmail("deleted-" + p.getId() + "@anonymized.local");
            p.setPhone(null);
            p.setAvatarUrl(null);
            customerProfileRepository.save(p);
            count++;
        }
        return count;
    }

    protected int deleteFeedback(Long tenantId) {
        return feedbackRepository.deleteByTenantId(tenantId);
    }

    protected int deleteMessages(Long tenantId) {
        return messageRepository.deleteByTenantId(tenantId);
    }

    protected int deleteConversations(Long tenantId) {
        return conversationRepository.deleteByTenantId(tenantId);
    }
}
