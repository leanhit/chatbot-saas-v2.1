package com.chatbot.shared.penny.context.storage;

import com.chatbot.config.StructuredLogging;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.shared.penny.context.ConversationContext;
import com.chatbot.shared.penny.dto.request.MiddlewareRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Database Context Storage - Lưu trữ context trong database cho persistence
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseContextStorage {
    
    private final ConversationRepository conversationRepository;
    
    /**
     * Load context from database
     */
    public ConversationContext loadContext(MiddlewareRequest request) {
        try {
            StructuredLogging.debugConditional("Loading context from database", "userId", request.getUserId(), "platform", request.getPlatform());
            
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                log.warn("No tenant context found, cannot load context");
                return null;
            }
            
            // Find conversation by external user ID and platform
            // For now, we'll use externalUserId as the primary key
            List<Conversation> conversations = conversationRepository.findByTenantId(tenantId);
            
            // Filter by external user ID and platform (if available)
            List<Conversation> userConversations = conversations.stream()
                .filter(c -> request.getUserId().equals(c.getExternalUserId()))
                .filter(c -> request.getPlatform() == null || 
                           (c.getChannel() != null && request.getPlatform().equalsIgnoreCase(c.getChannel().name())))
                .collect(Collectors.toList());
            
            if (userConversations.isEmpty()) {
                StructuredLogging.debugConditional("No existing conversation found for user", "userId", request.getUserId());
                return null;
            }
            
            // Get the most recent conversation
            Conversation latestConversation = userConversations.stream()
                .max((c1, c2) -> c1.getUpdatedAt().compareTo(c2.getUpdatedAt()))
                .orElse(null);
            
            if (latestConversation == null) {
                return null;
            }
            
            // Convert Conversation to ConversationContext
            return convertToConversationContext(latestConversation);
            
        } catch (Exception e) {
            log.error("❌ Error loading context from database: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Save new context to database
     */
    public void saveContext(ConversationContext context) {
        try {
            StructuredLogging.debugConditional("Saving new context to database", "contextId", context.getContextId());
            
            // In real implementation, this would:
            // 1. Create new Conversation entity
            // 2. Save Penny-specific fields
            // 3. Handle tenant isolation
            
        } catch (Exception e) {
            log.error("❌ Error saving context to database {}: {}", context.getContextId(), e.getMessage(), e);
        }
    }
    
    /**
     * Update existing context in database
     */
    public void updateContext(ConversationContext context) {
        try {
            StructuredLogging.debugConditional("Updating context in database", "contextId", context.getContextId());
            
            // In real implementation, this would:
            // 1. Update existing Conversation entity
            // 2. Update Penny-specific fields
            // 3. Maintain audit trail
            
        } catch (Exception e) {
            log.error("❌ Error updating context in database {}: {}", context.getContextId(), e.getMessage(), e);
        }
    }
    
    /**
     * Clear context from database
     */
    public void clearContext(String contextKey) {
        try {
            StructuredLogging.debugConditional("Clearing context from database", "contextKey", contextKey);
            
            // In real implementation, this would:
            // 1. Delete or archive conversation
            // 2. Clean up related data
            
        } catch (Exception e) {
            log.error("❌ Error clearing context from database {}: {}", contextKey, e.getMessage(), e);
        }
    }
    
    /**
     * Get active contexts for tenant
     */
    public List<ConversationContext> getActiveContexts(String tenantId) {
        try {
            StructuredLogging.debugConditional("Getting active contexts for tenant", "tenantId", tenantId);
            
            Long tenantIdLong = Long.parseLong(tenantId);
            List<Conversation> activeConversations = conversationRepository.findByTenantIdAndStatus(tenantIdLong, "open");
            
            return activeConversations.stream()
                .map(this::convertToConversationContext)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("❌ Error getting active contexts for tenant {}: {}", tenantId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Cleanup expired contexts
     */
    public int cleanupExpiredContexts() {
        try {
            StructuredLogging.debug("Cleaning up expired contexts in database");
            
            // In real implementation, this would:
            // 1. Find expired conversations
            // 2. Archive or delete them
            // 3. Return count of cleaned contexts
            
            return 0; // Return 0 for now
            
        } catch (Exception e) {
            log.error("❌ Error cleaning up expired contexts: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Get context statistics
     */
    public ContextStatistics getStatistics(String tenantId) {
        try {
            StructuredLogging.debugConditional("Getting context statistics for tenant", "tenantId", tenantId);
            
            // In real implementation, this would:
            // 1. Query conversation table for metrics
            // 2. Calculate statistics
            // 3. Return aggregated data
            
            return ContextStatistics.empty();
            
        } catch (Exception e) {
            log.error("❌ Error getting statistics for tenant {}: {}", tenantId, e.getMessage(), e);
            return ContextStatistics.empty();
        }
    }
    
    /**
     * Find context by user and platform
     */
    public ConversationContext findByUserAndPlatform(String userId, String platform, Long tenantId) {
        try {
            StructuredLogging.debugConditional("Finding context for user and platform", "userId", userId, "platform", platform);
            
            List<Conversation> conversations = conversationRepository.findByTenantId(tenantId);
            
            List<Conversation> userConversations = conversations.stream()
                .filter(c -> userId.equals(c.getExternalUserId()))
                .filter(c -> platform == null || (c.getChannel() != null && platform.equalsIgnoreCase(c.getChannel().name())))
                .collect(Collectors.toList());
            
            if (userConversations.isEmpty()) {
                return null;
            }
            
            // Get the most recent conversation
            Conversation latestConversation = userConversations.stream()
                .max((c1, c2) -> c1.getUpdatedAt().compareTo(c2.getUpdatedAt()))
                .orElse(null);
            
            return convertToConversationContext(latestConversation);
            
        } catch (Exception e) {
            log.error("❌ Error finding context for user {} on platform {}: {}", 
                userId, platform, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Get contexts by status
     */
    public List<ConversationContext> getContextsByStatus(String status, Long tenantId) {
        try {
            StructuredLogging.debugConditional("Getting contexts by status", "status", status, "tenantId", tenantId);
            
            List<Conversation> conversations = conversationRepository.findByTenantIdAndStatus(tenantId, status);
            
            return conversations.stream()
                .map(this::convertToConversationContext)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("❌ Error getting contexts by status {} for tenant {}: {}", 
                status, tenantId, e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Update context status
     */
    public void updateContextStatus(String contextId, String status) {
        try {
            StructuredLogging.debugConditional("Updating context status", "contextId", contextId, "status", status);
            
            Long conversationId = Long.parseLong(contextId);
            Long tenantId = TenantContext.getTenantId();
            
            if (tenantId == null) {
                log.warn("No tenant context found");
                return;
            }
            
            Conversation conversation = conversationRepository.findByIdAndTenantId(conversationId, tenantId)
                .orElse(null);
            
            if (conversation != null) {
                conversation.setStatus(status);
                conversation.setUpdatedAt(LocalDateTime.now());
                conversationRepository.save(conversation);
                StructuredLogging.debugConditional("Updated context status", "contextId", contextId, "status", status);
            }
            
        } catch (Exception e) {
            log.error("❌ Error updating context status {}: {}", contextId, e.getMessage(), e);
        }
    }
    
    /**
     * Get context by ID
     */
    public ConversationContext getContextById(String contextId) {
        try {
            StructuredLogging.debugConditional("Getting context by ID", "contextId", contextId);
            
            Long conversationId = Long.parseLong(contextId);
            Long tenantId = TenantContext.getTenantId();
            
            if (tenantId == null) {
                log.warn("No tenant context found");
                return null;
            }
            
            Conversation conversation = conversationRepository.findByIdAndTenantId(conversationId, tenantId)
                .orElse(null);
            
            return convertToConversationContext(conversation);
            
        } catch (Exception e) {
            log.error("❌ Error getting context by ID {}: {}", contextId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Batch update contexts
     */
    public void batchUpdateContexts(List<ConversationContext> contexts) {
        try {
            StructuredLogging.debugConditional("Batch updating contexts", "count", contexts.size());
            
            // In real implementation, this would use batch update
            
        } catch (Exception e) {
            log.error("❌ Error batch updating contexts: {}", e.getMessage(), e);
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
        
        // Getters and setters
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
    
    /**
     * Load context by bot ID
     */
    public ConversationContext loadContextByBotId(UUID botId) {
        try {
            StructuredLogging.debugConditional("Loading context by bot ID", "botId", botId.toString());
            
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                log.warn("No tenant context found");
                return null;
            }
            
            // Find conversations by tenant and filter by botId in metadata
            List<Conversation> conversations = conversationRepository.findByTenantId(tenantId);
            
            List<Conversation> botConversations = conversations.stream()
                .filter(c -> c.getCustomAttributes() != null && c.getCustomAttributes().contains(botId.toString()))
                .collect(Collectors.toList());
            
            if (botConversations.isEmpty()) {
                return null;
            }
            
            // Get the most recent conversation
            Conversation latestConversation = botConversations.stream()
                .max((c1, c2) -> c1.getUpdatedAt().compareTo(c2.getUpdatedAt()))
                .orElse(null);
            
            return convertToConversationContext(latestConversation);
            
        } catch (Exception e) {
            log.error("❌ Error loading context by bot ID {}: {}", botId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Delete context by bot ID
     */
    public void deleteContextByBotId(UUID botId) {
        try {
            StructuredLogging.debugConditional("Deleting context by bot ID", "botId", botId.toString());
            
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                log.warn("No tenant context found");
                return;
            }
            
            List<Conversation> conversations = conversationRepository.findByTenantId(tenantId);
            
            List<Conversation> botConversations = conversations.stream()
                .filter(c -> c.getCustomAttributes() != null && c.getCustomAttributes().contains(botId.toString()))
                .collect(Collectors.toList());
            
            for (Conversation conversation : botConversations) {
                conversationRepository.delete(conversation);
            }
            
            log.info("✅ Deleted {} contexts for bot ID: {}", botConversations.size(), botId);
            
        } catch (Exception e) {
            log.error("❌ Error deleting context by bot ID {}: {}", botId, e.getMessage());
        }
    }
    
    /**
     * Convert Conversation entity to ConversationContext
     */
    private ConversationContext convertToConversationContext(Conversation conversation) {
        if (conversation == null) {
            return null;
        }
        
        ConversationContext context = new ConversationContext();
        context.setContextId(conversation.getId().toString());
        context.setUserId(conversation.getExternalUserId());
        context.setPlatform(conversation.getChannel() != null ? conversation.getChannel().name() : "unknown");
        context.setTenantId(conversation.getTenantId());
        context.setCreatedAt(conversation.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
        context.setLastUpdated(conversation.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant());
        
        // Set additional context data from conversation
        if (conversation.getCustomAttributes() != null) {
            context.setMetadata(java.util.Map.of("customAttributes", conversation.getCustomAttributes()));
        }
        
        // Set status
        context.setStatus(conversation.getStatus());
        
        return context;
    }
}
