package com.chatbot.core.tenant.service;

import com.chatbot.core.tenant.dto.TenantStatsResponse;
import com.chatbot.core.tenant.exception.TenantNotFoundException;
import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.shared.penny.kb.KnowledgeArticleRepository;
import com.chatbot.shared.penny.repository.PennyBotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for aggregating tenant-level statistics for the Overview dashboard.
 *
 * <p>Data sources:
 * <ul>
 *   <li>Active Users  → TenantMemberRepository (ACTIVE members)</li>
 *   <li>Total Bots    → PennyBotRepository (active bots for the tenant)</li>
 *   <li>Storage Used  → KnowledgeArticleRepository (total KB content length)</li>
 *   <li>Total Messages→ MessageRepository (all messages scoped to tenant)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantStatsService {

    private final TenantRepository tenantRepository;
    private final TenantMemberRepository memberRepository;
    private final PennyBotRepository pennyBotRepository;
    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final MessageRepository messageRepository;

    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public TenantStatsResponse getTenantStats(String tenantKey) {
        Long tenantId = tenantRepository.findByTenantKey(tenantKey)
                .map(t -> t.getId())
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with key: " + tenantKey));

        try {
            // Active workspace members
            long activeUsers = memberRepository.countByTenant_IdAndStatus(tenantId, MembershipStatus.ACTIVE);

            // Active bots (Penny)
            long totalBots = pennyBotRepository.countActiveBotsByTenant(tenantId);

            // Total messages (proxy for API calls)
            long totalMessages = safeCount(() -> messageRepository.countByConversationTenantId(tenantId));

            // Storage: sum character lengths of knowledge articles, convert to bytes → human-readable
            long rawBytes = knowledgeArticleRepository.sumContentLengthByTenantId(tenantId);
            String storageUsed = formatBytes(rawBytes);

            log.info("📊 [TenantStats] tenantKey={} → users={}, bots={}, msgs={}, storage={}",
                    tenantKey, activeUsers, totalBots, totalMessages, storageUsed);

            return TenantStatsResponse.builder()
                    .activeUsers(activeUsers)
                    .totalBots(totalBots)
                    .totalMessages(totalMessages)
                    .storageUsed(storageUsed)
                    .build();

        } catch (Exception e) {
            log.error("❌ [TenantStats] Failed to compute stats for tenantKey={}: {}", tenantKey, e.getMessage(), e);
            return TenantStatsResponse.builder()
                    .activeUsers(0)
                    .totalBots(0)
                    .totalMessages(0)
                    .storageUsed("0 B")
                    .build();
        }
    }

    /**
     * Safely execute a count query that might fail due to a different datasource
     * (e.g. message store uses a different transaction manager).
     */
    private long safeCount(java.util.concurrent.Callable<Long> supplier) {
        try {
            Long result = supplier.call();
            return result != null ? result : 0L;
        } catch (Exception e) {
            log.warn("⚠️ [TenantStats] Count query failed, defaulting to 0: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * Format raw bytes to a human-readable size string (B / KB / MB / GB).
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
