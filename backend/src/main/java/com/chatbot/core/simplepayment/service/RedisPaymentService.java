package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.dto.PaymentEvent;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisPaymentService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis keys
    private static final String PAYMENT_KEY_PREFIX = "payment:";
    private static final String PAYMENT_QUEUE = "payment:events";
    private static final String PENDING_PAYMENTS_SET = "payments:pending";
    private static final String PAYMENT_STATUS_CHANNEL = "payment:status";

    /**
     * Publish payment event to Redis
     */
    public void publishPaymentEvent(PaymentEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            // Choose channel based on event type
            String channel = PAYMENT_STATUS_CHANNEL; // default
            if ("PAYMENT_SIMULATED".equals(event.getType())) {
                channel = "payment:simulated";
            }
            
            redisTemplate.convertAndSend(channel, eventJson);
            log.debug("📢 Published payment event: {} to channel: {}", event.getType(), channel);
        } catch (Exception e) {
            log.error("❌ Error publishing payment event: {}", e.getMessage(), e);
        }
    }

    /**
     * Store payment in Redis with TTL
     */
    public void storePayment(String referenceCode, PaymentEvent event) {
        try {
            String key = PAYMENT_KEY_PREFIX + referenceCode;
            String eventJson = objectMapper.writeValueAsString(event);
            redisTemplate.opsForValue().set(key, eventJson, Duration.ofHours(24));
            log.debug("💾 Stored payment in Redis: {}", referenceCode);
        } catch (Exception e) {
            log.error("❌ Error storing payment in Redis: {}", e.getMessage(), e);
        }
    }

    /**
     * Add to pending payments set for tracking
     */
    public void addToPendingPayments(String referenceCode) {
        try {
            redisTemplate.opsForSet().add(PENDING_PAYMENTS_SET, referenceCode);
            // Set TTL for the set entry
            redisTemplate.expire(PENDING_PAYMENTS_SET + ":" + referenceCode, Duration.ofHours(2));
            log.debug("➕ Added to pending payments: {}", referenceCode);
        } catch (Exception e) {
            log.error("❌ Error adding to pending payments: {}", e.getMessage(), e);
        }
    }

    /**
     * Remove from pending payments set
     */
    public void removeFromPendingPayments(String referenceCode) {
        try {
            redisTemplate.opsForSet().remove(PENDING_PAYMENTS_SET, referenceCode);
            log.debug("➖ Removed from pending payments: {}", referenceCode);
        } catch (Exception e) {
            log.error("❌ Error removing from pending payments: {}", e.getMessage(), e);
        }
    }

    /**
     * Get payment from Redis
     */
    public PaymentEvent getPayment(String referenceCode) {
        try {
            String key = PAYMENT_KEY_PREFIX + referenceCode;
            Object eventObj = redisTemplate.opsForValue().get(key);
            
            if (eventObj == null) {
                return null;
            }
            
            if (eventObj instanceof String) {
                return objectMapper.readValue((String) eventObj, PaymentEvent.class);
            } else {
                return objectMapper.convertValue(eventObj, PaymentEvent.class);
            }
        } catch (Exception e) {
            log.error("❌ Error getting payment from Redis: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Check if payment exists in Redis
     */
    public boolean paymentExists(String referenceCode) {
        try {
            String key = PAYMENT_KEY_PREFIX + referenceCode;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("❌ Error checking payment existence: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get count of pending payments
     */
    public long getPendingPaymentsCount() {
        try {
            return redisTemplate.opsForSet().size(PENDING_PAYMENTS_SET);
        } catch (Exception e) {
            log.error("❌ Error getting pending payments count: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Create payment event for new payment
     */
    public PaymentEvent createPaymentEvent(String referenceCode, Long userId, Long tenantId, 
                                          String amount, String currency, String description) {
        return PaymentEvent.builder()
                .referenceCode(referenceCode)
                .userId(userId)
                .tenantId(tenantId)
                .amount(amount)
                .currency(currency)
                .description(description)
                .status(PaymentStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .type("PAYMENT_CREATED")
                .build();
    }

    /**
     * Create payment event for status update
     */
    public PaymentEvent createStatusUpdateEvent(String referenceCode, PaymentStatus status, 
                                               String bankTransactionId) {
        return PaymentEvent.builder()
                .referenceCode(referenceCode)
                .status(status.name())
                .bankTransactionId(bankTransactionId)
                .updatedAt(LocalDateTime.now())
                .type(status == PaymentStatus.COMPLETED ? "PAYMENT_COMPLETED" : 
                     status == PaymentStatus.EXPIRED ? "PAYMENT_EXPIRED" : "PAYMENT_UPDATED")
                .build();
    }
}
