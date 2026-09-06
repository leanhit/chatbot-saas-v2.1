package com.chatbot.core.payment.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankApiService {

    private final RedisTemplate<String, Object> redisTemplate;
    // private final RedisPaymentService redisPaymentService;
    // private final SimplePaymentRepository paymentRepository;

    // Mock bank transaction storage for development
    private final Map<String, BankTransaction> mockTransactions = new ConcurrentHashMap<>();

    /**
     * Find bank transaction by reference code
     * This is a mock implementation for development
     */
    public String findTransactionByReference(String referenceCode) {
        log.info("🏦 Finding bank transaction for reference: {}", referenceCode);

        // Check mock storage
        BankTransaction transaction = mockTransactions.get(referenceCode);
        if (transaction != null && transaction.isCompleted()) {
            log.info("✅ Found completed transaction: {}", transaction.getTransactionId());
            return transaction.getTransactionId();
        }

        // Check Redis for real-time transactions
        String redisKey = "bank:transaction:" + referenceCode;
        Object transactionData = redisTemplate.opsForValue().get(redisKey);
        if (transactionData != null) {
            log.info("✅ Found transaction in Redis: {}", referenceCode);
            return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        log.debug("💳 No transaction found for reference: {}", referenceCode);
        return null;
    }

    /**
     * Simulate a bank transaction
     * This is for testing purposes
     */
    public String simulateTransaction(String referenceCode, BigDecimal amount, String description) {
        log.info("🧪 Simulating bank transaction: {}, amount: {}", referenceCode, amount);

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        BankTransaction transaction = BankTransaction.builder()
                .transactionId(transactionId)
                .referenceCode(referenceCode)
                .amount(amount)
                .description(description)
                .status("COMPLETED")
                .completedAt(LocalDateTime.now())
                .build();

        mockTransactions.put(referenceCode, transaction);

        // Store in Redis for real-time checking
        String redisKey = "bank:transaction:" + referenceCode;
        redisTemplate.opsForValue().set(redisKey, transaction, java.time.Duration.ofHours(24));

        log.info("✅ Simulated transaction created: {}", transactionId);
        return transactionId;
    }

    /**
     * Validate bank transaction
     */
    public boolean validateTransaction(String transactionId) {
        log.info("🏦 Validating bank transaction: {}", transactionId);

        // Check mock storage
        for (BankTransaction transaction : mockTransactions.values()) {
            if (transaction.getTransactionId().equals(transactionId) && transaction.isCompleted()) {
                return true;
            }
        }

        // In production, this would call the actual bank API
        return false;
    }

    /**
     * Get transaction details
     */
    public Map<String, Object> getTransactionDetails(String transactionId) {
        log.info("🏦 Getting transaction details: {}", transactionId);

        for (BankTransaction transaction : mockTransactions.values()) {
            if (transaction.getTransactionId().equals(transactionId)) {
                return Map.of(
                    "transactionId", transaction.getTransactionId(),
                    "referenceCode", transaction.getReferenceCode(),
                    "amount", transaction.getAmount(),
                    "description", transaction.getDescription(),
                    "status", transaction.getStatus(),
                    "completedAt", transaction.getCompletedAt()
                );
            }
        }

        return null;
    }

    /**
     * Clear mock transactions (for testing)
     */
    public void clearMockTransactions() {
        mockTransactions.clear();
        log.info("🧹 Cleared mock transactions");
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class BankTransaction {
        private String transactionId;
        private String referenceCode;
        private BigDecimal amount;
        private String description;
        private String status;
        private LocalDateTime completedAt;

        public boolean isCompleted() {
            return "COMPLETED".equals(status);
        }
    }
}
