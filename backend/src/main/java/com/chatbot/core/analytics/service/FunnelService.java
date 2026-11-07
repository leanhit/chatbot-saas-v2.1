package com.chatbot.core.analytics.service;

import com.chatbot.core.analytics.model.FunnelStage;
import com.chatbot.core.analytics.repository.FunnelStageRepository;
import com.chatbot.core.customer.repository.FeedbackRepository;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Funnel analytics: tenant-configurable stages, counts computed from
 * tenant-scoped conversations/feedback. Stages are ordered; conversion
 * between adjacent stages is reported.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FunnelService {

    private final FunnelStageRepository funnelStageRepository;
    private final ConversationRepository conversationRepository;
    private final FeedbackRepository feedbackRepository;

    public record FunnelStageResult(Long id, String name, int order, long count,
                                    Double conversionFromPrevious,
                                    FunnelStage.MatchType matchType, String matchValue) {}

    // ---------- Stage config CRUD (tenant-scoped) ----------

    @Transactional(readOnly = true)
    public List<FunnelStage> listStages(Long tenantId) {
        return funnelStageRepository.findByTenantIdOrderByStageOrderAsc(tenantId);
    }

    @Transactional
    public FunnelStage createStage(FunnelStage stage, Long tenantId) {
        stage.setTenantId(tenantId);
        if (stage.getActive() == null) stage.setActive(true);
        return funnelStageRepository.save(stage);
    }

    @Transactional
    public FunnelStage updateStage(Long id, FunnelStage update, Long tenantId) {
        FunnelStage stage = funnelStageRepository.findById(id)
            .filter(s -> tenantId.equals(s.getTenantId()))
            .orElseThrow(() -> new IllegalArgumentException("Funnel stage not found"));
        if (update.getName() != null) stage.setName(update.getName());
        if (update.getStageOrder() != null) stage.setStageOrder(update.getStageOrder());
        if (update.getMatchType() != null) stage.setMatchType(update.getMatchType());
        if (update.getMatchValue() != null) stage.setMatchValue(update.getMatchValue());
        if (update.getActive() != null) stage.setActive(update.getActive());
        return funnelStageRepository.save(stage);
    }

    @Transactional
    public void deleteStage(Long id, Long tenantId) {
        FunnelStage stage = funnelStageRepository.findById(id)
            .filter(s -> tenantId.equals(s.getTenantId()))
            .orElseThrow(() -> new IllegalArgumentException("Funnel stage not found"));
        funnelStageRepository.delete(stage);
    }

    // ---------- Funnel computation ----------

    @Transactional(readOnly = true)
    public List<FunnelStageResult> computeFunnel(Long tenantId) {
        List<FunnelStage> stages = funnelStageRepository
            .findByTenantIdAndActiveTrueOrderByStageOrderAsc(tenantId);
        if (stages.isEmpty()) return List.of();

        List<Conversation> conversations = conversationRepository.findByTenantId(tenantId);
        Set<Long> conversationIdsWithFeedback = feedbackRepository.findByTenantId(tenantId).stream()
            .map(com.chatbot.core.customer.model.Feedback::getConversationId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        List<FunnelStageResult> results = new ArrayList<>();
        long previousCount = -1;
        for (FunnelStage stage : stages) {
            long count = conversations.stream()
                .filter(c -> matches(stage, c, conversationIdsWithFeedback))
                .count();
            Double conversion = previousCount > 0 ? (double) count / previousCount : null;
            results.add(new FunnelStageResult(stage.getId(), stage.getName(),
                stage.getStageOrder(), count, conversion,
                stage.getMatchType(), stage.getMatchValue()));
            previousCount = count;
        }
        return results;
    }

    private boolean matches(FunnelStage stage, Conversation c, Set<Long> conversationIdsWithFeedback) {
        return switch (stage.getMatchType()) {
            case STATUS -> stage.getMatchValue() != null
                && stage.getMatchValue().equalsIgnoreCase(String.valueOf(c.getStatus()));
            case ESCALATED -> c.getCurrentEscalationTier() != null;
            case FEEDBACK -> conversationIdsWithFeedback.contains(c.getId());
            case AGENT_ASSIGNED -> c.getAgentAssignedId() != null;
        };
    }
}
