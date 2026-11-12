package com.chatbot.core.penny.context.storage;

import com.chatbot.config.StructuredLogging;
import com.chatbot.core.penny.context.ConversationContext;
import com.chatbot.core.penny.dto.request.MiddlewareRequest;
import com.chatbot.core.penny.model.PennyConversationContext;
import com.chatbot.core.penny.repository.PennyConversationContextRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Database Context Storage - Persistent storage for Penny conversation contexts.
 *
 * Context identity: tenantId + botId + userId + platform (contextKey).
 * Each context is stored as a serialized ConversationContext JSON blob so the
 * full state (history, intents, preferences, variables) survives Redis eviction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseContextStorage {

    private final PennyConversationContextRepository contextRepository;
    private final ObjectMapper objectMapper;

    /**
     * Load context for a middleware request (tenant + bot + user + platform).
     */
    public ConversationContext loadContext(MiddlewareRequest request) {
        try {
            StructuredLogging.debugConditional("Loading context from database",
                "userId", request.getUserId(), "platform", request.getPlatform(), "botId", request.getBotId());

            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                log.warn("No tenant context found, cannot load context");
                return null;
            }

            Optional<PennyConversationContext> entity = contextRepository
                .findByTenantIdAndBotIdAndUserIdAndPlatform(
                    tenantId, request.getBotId(), request.getUserId(), request.getPlatform());

            return entity.map(this::deserialize).orElse(null);

        } catch (Exception e) {
            log.error("❌ Error loading context from database: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Save (insert or update) a conversation context.
     */
    @Transactional
    public void saveContext(ConversationContext context) {
        try {
            StructuredLogging.debugConditional("Saving context to database", "contextId", context.getContextId());

            Long tenantId = resolveTenantId(context);
            if (tenantId == null) {
                log.warn("No tenant context found, skipping DB save for context {}", context.getContextId());
                return;
            }

            String contextKey = buildContextKey(context);
            Instant now = Instant.now();

            PennyConversationContext entity = contextRepository.findByContextKey(contextKey)
                .orElseGet(() -> PennyConversationContext.builder()
                    .id(UUID.randomUUID())
                    .contextKey(contextKey)
                    .createdAt(now)
                    .build());

            entity.setTenantId(tenantId);
            entity.setBotId(context.getBotId());
            entity.setUserId(context.getUserId());
            entity.setPlatform(context.getPlatform());
            entity.setStatus(context.getStatus());
            entity.setMessageCount(context.getMessageCount());
            entity.setLastIntent(context.getLastIntent());
            entity.setContextData(serialize(context));
            entity.setLastUpdated(now);
            entity.setExpiresAt(context.getExpiresAt());

            contextRepository.save(entity);

        } catch (Exception e) {
            log.error("❌ Error saving context to database {}: {}", context.getContextId(), e.getMessage(), e);
        }
    }

    /**
     * Update existing context in database (upsert semantics).
     */
    @Transactional
    public void updateContext(ConversationContext context) {
        saveContext(context);
    }

    /**
     * Clear context from database by context key.
     */
    @Transactional
    public void clearContext(String contextKey) {
        try {
            StructuredLogging.debugConditional("Clearing context from database", "contextKey", contextKey);
            contextRepository.deleteByContextKey(contextKey);
        } catch (Exception e) {
            log.error("❌ Error clearing context from database {}: {}", contextKey, e.getMessage(), e);
        }
    }

    /**
     * Get active contexts for tenant.
     */
    public List<ConversationContext> getActiveContexts(String tenantId) {
        try {
            StructuredLogging.debugConditional("Getting active contexts for tenant", "tenantId", tenantId);
            return contextRepository.findByTenantIdAndStatus(Long.parseLong(tenantId), "active")
                .stream()
                .map(this::deserialize)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Error getting active contexts for tenant {}: {}", tenantId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Cleanup expired contexts across all tenants (scheduled job).
     */
    @Transactional
    public int cleanupExpiredContexts() {
        try {
            StructuredLogging.debug("Cleaning up expired contexts in database");
            List<PennyConversationContext> expired = contextRepository.findByExpiresAtBefore(Instant.now());
            int count = 0;
            for (PennyConversationContext entity : expired) {
                if (!"expired".equals(entity.getStatus())) {
                    entity.setStatus("expired");
                    entity.setLastUpdated(Instant.now());
                    contextRepository.save(entity);
                    count++;
                }
            }
            log.info("✅ Marked {} contexts as expired", count);
            return count;
        } catch (Exception e) {
            log.error("❌ Error cleaning up expired contexts: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Get context statistics for tenant.
     */
    public ContextStatistics getStatistics(String tenantId) {
        try {
            StructuredLogging.debugConditional("Getting context statistics for tenant", "tenantId", tenantId);
            Long tenantIdLong = Long.parseLong(tenantId);

            ContextStatistics stats = new ContextStatistics();
            stats.setTotalContexts(contextRepository.countByTenantId(tenantIdLong));
            stats.setActiveContexts(contextRepository.countByTenantIdAndStatus(tenantIdLong, "active"));
            stats.setExpiredContexts(contextRepository.countByTenantIdAndStatus(tenantIdLong, "expired"));
            stats.setTotalMessages(contextRepository.sumMessageCountByTenantId(tenantIdLong));
            stats.setLastUpdated(contextRepository.findLastUpdatedByTenantId(tenantIdLong).orElse(null));

            if (stats.getTotalContexts() > 0) {
                stats.setAverageMessagesPerContext(
                    (double) stats.getTotalMessages() / stats.getTotalContexts());
            }
            return stats;
        } catch (Exception e) {
            log.error("❌ Error getting statistics for tenant {}: {}", tenantId, e.getMessage(), e);
            return ContextStatistics.empty();
        }
    }

    /**
     * Find context by user and platform (latest across bots of the tenant).
     */
    public ConversationContext findByUserAndPlatform(String userId, String platform, Long tenantId) {
        try {
            StructuredLogging.debugConditional("Finding context for user and platform",
                "userId", userId, "platform", platform);

            List<PennyConversationContext> matches = contextRepository
                .findByTenantIdAndUserIdAndPlatform(tenantId, userId, platform);

            return matches.stream()
                .max((a, b) -> a.getLastUpdated().compareTo(b.getLastUpdated()))
                .map(this::deserialize)
                .orElse(null);
        } catch (Exception e) {
            log.error("❌ Error finding context for user {} on platform {}: {}",
                userId, platform, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get contexts by status for tenant.
     */
    public List<ConversationContext> getContextsByStatus(String status, Long tenantId) {
        try {
            StructuredLogging.debugConditional("Getting contexts by status", "status", status, "tenantId", tenantId);
            return contextRepository.findByTenantIdAndStatus(tenantId, status.toLowerCase())
                .stream()
                .map(this::deserialize)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Error getting contexts by status {} for tenant {}: {}",
                status, tenantId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Update context status by context ID (entity UUID).
     */
    @Transactional
    public void updateContextStatus(String contextId, String status) {
        try {
            StructuredLogging.debugConditional("Updating context status", "contextId", contextId, "status", status);

            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                log.warn("No tenant context found");
                return;
            }

            UUID id = UUID.fromString(contextId);
            contextRepository.findById(id)
                .filter(entity -> tenantId.equals(entity.getTenantId()))
                .ifPresent(entity -> {
                    entity.setStatus(status);
                    entity.setLastUpdated(Instant.now());
                    contextRepository.save(entity);
                });
        } catch (Exception e) {
            log.error("❌ Error updating context status {}: {}", contextId, e.getMessage(), e);
        }
    }

    /**
     * Get context by ID (entity UUID), tenant-scoped.
     */
    public ConversationContext getContextById(String contextId) {
        try {
            StructuredLogging.debugConditional("Getting context by ID", "contextId", contextId);

            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                log.warn("No tenant context found");
                return null;
            }

            return contextRepository.findById(UUID.fromString(contextId))
                .filter(entity -> tenantId.equals(entity.getTenantId()))
                .map(this::deserialize)
                .orElse(null);
        } catch (Exception e) {
            log.error("❌ Error getting context by ID {}: {}", contextId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Batch update contexts.
     */
    @Transactional
    public void batchUpdateContexts(List<ConversationContext> contexts) {
        try {
            StructuredLogging.debugConditional("Batch updating contexts", "count", contexts.size());
            for (ConversationContext context : contexts) {
                saveContext(context);
            }
        } catch (Exception e) {
            log.error("❌ Error batch updating contexts: {}", e.getMessage(), e);
        }
    }

    /**
     * Load latest context for a bot (used for bot health checks).
     */
    public ConversationContext loadContextByBotId(UUID botId) {
        try {
            StructuredLogging.debugConditional("Loading context by bot ID", "botId", botId.toString());

            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                log.warn("No tenant context found");
                return null;
            }

            return contextRepository
                .findFirstByTenantIdAndBotIdOrderByLastUpdatedDesc(tenantId, botId.toString())
                .map(this::deserialize)
                .orElse(null);
        } catch (Exception e) {
            log.error("❌ Error loading context by bot ID {}: {}", botId, e.getMessage());
            return null;
        }
    }

    /**
     * Delete all contexts for a bot (tenant-scoped).
     */
    @Transactional
    public void deleteContextByBotId(UUID botId) {
        try {
            StructuredLogging.debugConditional("Deleting context by bot ID", "botId", botId.toString());

            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                log.warn("No tenant context found");
                return;
            }

            contextRepository.deleteByTenantIdAndBotId(tenantId, botId.toString());
            log.info("✅ Deleted contexts for bot ID: {}", botId);
        } catch (Exception e) {
            log.error("❌ Error deleting context by bot ID {}: {}", botId, e.getMessage());
        }
    }

    // ===== Helpers =====

    private Long resolveTenantId(ConversationContext context) {
        if (context.getTenantId() != null) {
            return context.getTenantId();
        }
        return TenantContext.getTenantId();
    }

    private String buildContextKey(ConversationContext context) {
        return String.format("penny:ctx:%s:%s:%s",
            context.getBotId(), context.getUserId(), context.getPlatform());
    }

    private String serialize(ConversationContext context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (Exception e) {
            log.error("❌ Failed to serialize context {}: {}", context.getContextId(), e.getMessage());
            return null;
        }
    }

    private ConversationContext deserialize(PennyConversationContext entity) {
        if (entity.getContextData() == null || entity.getContextData().isBlank()) {
            return null;
        }
        try {
            ConversationContext context = objectMapper.readValue(entity.getContextData(), ConversationContext.class);
            // Ensure identity fields are authoritative from columns
            context.setTenantId(entity.getTenantId());
            context.setBotId(entity.getBotId());
            context.setUserId(entity.getUserId());
            context.setPlatform(entity.getPlatform());
            context.setStatus(entity.getStatus());
            return context;
        } catch (Exception e) {
            log.error("❌ Failed to deserialize context {}: {}", entity.getContextKey(), e.getMessage());
            return null;
        }
    }

    // Inner class for statistics
    public static class ContextStatistics {
        private long totalContexts;
        private long activeContexts;
        private long expiredContexts;
        private double averageMessagesPerContext;
        private long totalMessages;
        private String mostUsedProvider;
        private String mostCommonIntent;
        private Instant lastUpdated;

        public static ContextStatistics empty() {
            return new ContextStatistics();
        }

        public long getTotalContexts() { return totalContexts; }
        public void setTotalContexts(long totalContexts) { this.totalContexts = totalContexts; }

        public long getActiveContexts() { return activeContexts; }
        public void setActiveContexts(long activeContexts) { this.activeContexts = activeContexts; }

        public long getExpiredContexts() { return expiredContexts; }
        public void setExpiredContexts(long expiredContexts) { this.expiredContexts = expiredContexts; }

        public double getAverageMessagesPerContext() { return averageMessagesPerContext; }
        public void setAverageMessagesPerContext(double averageMessagesPerContext) { this.averageMessagesPerContext = averageMessagesPerContext; }

        public long getTotalMessages() { return totalMessages; }
        public void setTotalMessages(long totalMessages) { this.totalMessages = totalMessages; }

        public String getMostUsedProvider() { return mostUsedProvider; }
        public void setMostUsedProvider(String mostUsedProvider) { this.mostUsedProvider = mostUsedProvider; }

        public String getMostCommonIntent() { return mostCommonIntent; }
        public void setMostCommonIntent(String mostCommonIntent) { this.mostCommonIntent = mostCommonIntent; }

        public Instant getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}
