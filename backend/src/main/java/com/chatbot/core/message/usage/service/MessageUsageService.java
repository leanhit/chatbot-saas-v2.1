package com.chatbot.core.message.usage.service;

import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.tenant.service.TenantPackageService;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.model.Tenant;
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
    private final TenantRepository tenantRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private LocalDateTime getBillingPeriodStart(Long tenantId) {
        LocalDateTime now = LocalDateTime.now();
        try {
            Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
            if (tenant != null && tenant.getPackageActivatedAt() != null) {
                LocalDateTime activatedAt = tenant.getPackageActivatedAt();
                int activationDay = activatedAt.getDayOfMonth();
                int maxDayInMonth = java.time.YearMonth.from(now).lengthOfMonth();
                int targetDay = Math.min(activationDay, maxDayInMonth);
                
                LocalDateTime periodStart;
                if (now.getDayOfMonth() >= targetDay) {
                    periodStart = now.withDayOfMonth(targetDay).withHour(activatedAt.getHour()).withMinute(activatedAt.getMinute()).withSecond(activatedAt.getSecond()).withNano(0);
                } else {
                    LocalDateTime lastMonth = now.minusMonths(1);
                    int maxDayInLastMonth = java.time.YearMonth.from(lastMonth).lengthOfMonth();
                    int targetDayLastMonth = Math.min(activationDay, maxDayInLastMonth);
                    periodStart = lastMonth.withDayOfMonth(targetDayLastMonth).withHour(activatedAt.getHour()).withMinute(activatedAt.getMinute()).withSecond(activatedAt.getSecond()).withNano(0);
                }
                return periodStart;
            }
        } catch (Exception e) {
            log.warn("Error getting billing period start for tenant {}: {}", tenantId, e.getMessage());
        }
        return now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    private String getMessageCountKey(Long tenantId) {
        LocalDateTime periodStart = getBillingPeriodStart(tenantId);
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss");
        String period = periodStart.format(formatter);
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
            log.debug("[MessageUsageService] Evicted message count key: {}", key);
        } catch (Exception e) {
            log.error("Failed to evict message count in Redis: {}", e.getMessage());
        }
    }

    /**
     * Get current message usage for tenant in current billing period
     */
    @Transactional(value = "tenantTransactionManager", readOnly = true, rollbackFor = Exception.class)
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

        // Get start of current billing period
        LocalDateTime periodStart = getBillingPeriodStart(tenantId);
        
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

        boolean isUnlimited = currentPackage.getMessageLimit() == -1 || currentPackage.getMessageLimit() >= Integer.MAX_VALUE;
        
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
    @Transactional(value = "tenantTransactionManager", readOnly = true, rollbackFor = Exception.class)
    public boolean canSendMoreMessages(Long tenantId) {
        MessageUsageInfo usage = getCurrentUsage(tenantId);
        return usage.getCanSendMore();
    }

    /**
     * Validate message sending and increment atomically to prevent TOCTOU
     */
    @Transactional(value = "tenantTransactionManager", rollbackFor = Exception.class)
    public void validateAndIncrementMessageCount(Long tenantId) {
        MessageUsageInfo usage = getCurrentUsage(tenantId);
        
        if (usage.getIsUnlimited()) {
            incrementMessageCount(tenantId);
            return;
        }
        
        String key = getMessageCountKey(tenantId);
        Long newCount = redisTemplate.opsForValue().increment(key);
        
        if (newCount != null && newCount == 1) {
            redisTemplate.expire(key, java.time.Duration.ofDays(32));
        }
        
        if (newCount != null && newCount > usage.getTotalLimit()) {
            redisTemplate.opsForValue().decrement(key); // Rollback increment
            
            String message = String.format(
                "❌ Message limit exceeded! Your %s package allows %d messages per billing cycle. You have used %d messages.", 
                usage.getPackageName(), 
                usage.getTotalLimit(), 
                usage.getTotalLimit()
            );
            
            throw new MessageLimitExceededException(message);
        }
    }

    /**
     * Get message usage statistics for dashboard
     */
    @Transactional(value = "tenantTransactionManager", readOnly = true, rollbackFor = Exception.class)
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
                .isUnlimited(currentPackage.getMessageLimit() == -1 || currentPackage.getMessageLimit() >= Integer.MAX_VALUE)
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
