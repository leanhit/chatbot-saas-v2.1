package com.chatbot.core.message.usage.service;

import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.tenant.service.TenantPackageService;
import com.chatbot.core.message.store.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageUsageService {

    private final MessageRepository messageRepository;
    private final TenantPackageService tenantPackageService;
    private final RedisTemplate<String, String> redisTemplate;

    private String getMessageCountKey(Long tenantId) {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");
        String period = java.time.LocalDate.now().format(formatter);
        return "tenant:" + tenantId + ":message_count:" + period;
    }

    /**
     * Increment message count for tenant (Redis only)
     */
    public void incrementMessageCount(Long tenantId) {
        try {
            String key = getMessageCountKey(tenantId);
            redisTemplate.opsForValue().increment(key);
            log.debug("📈 [MessageUsageService] Incremented message count key: {}", key);
        } catch (Exception e) {
            log.error("Failed to increment message count in Redis: {}", e.getMessage());
        }
    }

    /**
     * Invalidate message count cache (Redis only)
     */
    public void evictMessageCountCache(Long tenantId) {
        try {
            String key = getMessageCountKey(tenantId);
            redisTemplate.delete(key);
            log.debug("🧹 [MessageUsageService] Evicted message count key: {}", key);
        } catch (Exception e) {
            log.error("Failed to evict message count in Redis: {}", e.getMessage());
        }
    }

    /**
     * Get current message usage for tenant in current billing period
     */
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public MessageUsageInfo getCurrentUsage(Long tenantId) {
        Package currentPackage = tenantPackageService.getCurrentTenantPackage(tenantId);
        if (currentPackage == null) {
            return MessageUsageInfo.builder()
                    .tenantId(tenantId)
                    .packageName("No Package")
                    .totalLimit(0)
                    .currentCount(0)
                    .remainingMessages(0)
                    .canSendMore(false)
                    .isUnlimited(false)
                    .build();
        }

        // Get start of current billing period (simplified: start of current month)
        LocalDateTime periodStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        String key = getMessageCountKey(tenantId);
        String cachedCount = redisTemplate.opsForValue().get(key);
        Long currentCount;

        if (cachedCount != null) {
            try {
                currentCount = Long.parseLong(cachedCount);
            } catch (NumberFormatException e) {
                log.warn("Invalid message count in Redis cache for tenant {}: {}", tenantId, cachedCount);
                currentCount = messageRepository.countByConversationTenantIdAndCreatedAtAfter(tenantId, periodStart);
                redisTemplate.opsForValue().set(key, String.valueOf(currentCount), java.time.Duration.ofDays(32));
            }
        } else {
            currentCount = messageRepository.countByConversationTenantIdAndCreatedAtAfter(tenantId, periodStart);
            redisTemplate.opsForValue().set(key, String.valueOf(currentCount), java.time.Duration.ofDays(32));
        }

        boolean isUnlimited = currentPackage.getMessageLimit() >= Integer.MAX_VALUE;
        
        int remaining;
        if (isUnlimited) {
            remaining = Integer.MAX_VALUE;
        } else {
            remaining = Math.max(0, currentPackage.getMessageLimit() - currentCount.intValue());
        }

        return MessageUsageInfo.builder()
                .tenantId(tenantId)
                .packageName(currentPackage.getName())
                .packageId(currentPackage.getPackageId())
                .totalLimit(currentPackage.getMessageLimit())
                .currentCount(currentCount.intValue())
                .remainingMessages(remaining)
                .canSendMore(isUnlimited || remaining > 0)
                .isUnlimited(isUnlimited)
                .periodStart(periodStart)
                .build();
    }

    /**
     * Check if tenant can send more messages
     */
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public boolean canSendMoreMessages(Long tenantId) {
        MessageUsageInfo usage = getCurrentUsage(tenantId);
        return usage.getCanSendMore();
    }

    /**
     * Validate message sending and throw exception if limit exceeded
     */
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public void validateMessageSending(Long tenantId) {
        if (!canSendMoreMessages(tenantId)) {
            MessageUsageInfo usage = getCurrentUsage(tenantId);
            
            String message;
            if (usage.getIsUnlimited()) {
                message = "Your package allows unlimited messages. You should be able to send more.";
            } else {
                message = String.format(
                    "❌ Message limit exceeded! Your %s package allows %d messages per month. You have used %d messages. Remaining: %d", 
                    usage.getPackageName(), 
                    usage.getTotalLimit(), 
                    usage.getCurrentCount(),
                    usage.getRemainingMessages()
                );
            }
            
            throw new MessageLimitExceededException(message);
        }
    }

    /**
     * Get message usage statistics for dashboard
     */
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public MessageUsageStats getUsageStats(Long tenantId) {
        Package currentPackage = tenantPackageService.getCurrentTenantPackage(tenantId);
        if (currentPackage == null) {
            return MessageUsageStats.empty(tenantId);
        }

        // Get usage for different periods
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime weekStart = now.minusWeeks(1);
        LocalDateTime dayStart = now.toLocalDate().atStartOfDay();

        Long totalMessages = messageRepository.countByConversationTenantId(tenantId);
        Long thisMonth = messageRepository.countByConversationTenantIdAndCreatedAtAfter(tenantId, monthStart);
        Long thisWeek = messageRepository.countByConversationTenantIdAndCreatedAtAfter(tenantId, weekStart);
        Long today = messageRepository.countByConversationTenantIdAndCreatedAtAfter(tenantId, dayStart);

        return MessageUsageStats.builder()
                .tenantId(tenantId)
                .packageName(currentPackage.getName())
                .packageId(currentPackage.getPackageId())
                .monthlyLimit(currentPackage.getMessageLimit())
                .totalMessages(totalMessages.intValue())
                .thisMonth(thisMonth.intValue())
                .thisWeek(thisWeek.intValue())
                .today(today.intValue())
                .isUnlimited(currentPackage.getMessageLimit() >= Integer.MAX_VALUE)
                .build();
    }

    /**
     * DTO for message usage information
     */
    @lombok.Data
    @lombok.Builder
    public static class MessageUsageInfo {
        private Long tenantId;
        private String packageName;
        private String packageId;
        private Integer totalLimit;
        private Integer currentCount;
        private Integer remainingMessages;
        private Boolean canSendMore;
        private Boolean isUnlimited;
        private LocalDateTime periodStart;
    }

    /**
     * DTO for message usage statistics
     */
    @lombok.Data
    @lombok.Builder
    public static class MessageUsageStats {
        private Long tenantId;
        private String packageName;
        private String packageId;
        private Integer monthlyLimit;
        private Integer totalMessages;
        private Integer thisMonth;
        private Integer thisWeek;
        private Integer today;
        private Boolean isUnlimited;

        public static MessageUsageStats empty(Long tenantId) {
            return MessageUsageStats.builder()
                    .tenantId(tenantId)
                    .packageName("No Package")
                    .packageId("none")
                    .monthlyLimit(0)
                    .totalMessages(0)
                    .thisMonth(0)
                    .thisWeek(0)
                    .today(0)
                    .isUnlimited(false)
                    .build();
        }
    }

    /**
     * Custom exception for message limit exceeded
     */
    public static class MessageLimitExceededException extends RuntimeException {
        public MessageLimitExceededException(String message) {
            super(message);
        }
    }
}
