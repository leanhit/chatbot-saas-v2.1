package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class BankApiService {

    // Mock database for demo - in real implementation, call actual bank API
    private final Map<String, BankTransaction> mockTransactionDatabase = new ConcurrentHashMap<>();

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisPaymentService redisPaymentService;
    private final SimplePaymentRepository paymentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BankApiService(
            RedisTemplate<String, Object> redisTemplate,
            RedisPaymentService redisPaymentService,
            SimplePaymentRepository paymentRepository) {
        this.redisTemplate = redisTemplate;
        this.redisPaymentService = redisPaymentService;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Find transaction by reference code.
     * Only returns transactions that were explicitly simulated via simulateBankTransaction().
     * In real production, this should call the actual bank's webhook/polling API.
     */
    public String findTransactionByReference(String referenceCode) {
        log.debug("🏦 Checking bank mock database for reference: {}", referenceCode);

        BankTransaction transaction = mockTransactionDatabase.get(referenceCode);

        if (transaction != null && !transaction.isProcessed()) {
            log.info("✅ Found bank transaction: {} for reference: {}", transaction.getTransactionId(), referenceCode);
            return transaction.getTransactionId();
        }

        log.debug("📭 No transaction found for reference: {}", referenceCode);
        return null;
    }

    /**
     * Mark a transaction as processed in mock bank database
     */
    public void markTransactionAsProcessed(String referenceCode) {
        BankTransaction transaction = mockTransactionDatabase.get(referenceCode);
        if (transaction != null) {
            transaction.setProcessed(true);
            log.info("🏦 Marked bank transaction as processed: {}", referenceCode);
        }
    }

    /**
     * Get recent unprocessed transactions from mock database.
     * In real production, this should call the actual bank's API.
     */
    public List<BankTransaction> getRecentTransactions() {
        List<BankTransaction> transactions = mockTransactionDatabase.values().stream()
                .filter(tx -> !tx.isProcessed())
                .toList();
        log.debug("🏦 Mock DB has {} unprocessed transactions", transactions.size());
        return transactions;
    }

    /**
     * Validate that the given transaction exists in mock database with matching reference.
     * In real production, this should verify against the bank's API.
     */
    public boolean validateTransaction(String transactionId, String referenceCode, BigDecimal amount) {
        BankTransaction transaction = mockTransactionDatabase.get(referenceCode);
        boolean isValid = transaction != null && transaction.getTransactionId().equals(transactionId);
        log.debug("🔍 Validating transaction {} for reference {}: {}", transactionId, referenceCode, isValid);
        return isValid;
    }

    /**
     * Get mock bank balance.
     * In real production, this should call the actual bank's balance API.
     */
    public BigDecimal getBankBalance() {
        BigDecimal balance = new BigDecimal("500000000"); // 500 million VND (mock)
        log.debug("💰 Mock bank balance: {}", balance);
        return balance;
    }

    /**
     * Test method to simulate a bank transaction
     */
    public void simulateBankTransaction(String referenceCode, BigDecimal amount) {
        log.info("🧪 Simulating bank transaction: {} amount: {}", referenceCode, amount);

        String transactionId = "BANK" + System.currentTimeMillis();
        BankTransaction transaction = new BankTransaction(transactionId, referenceCode, amount, LocalDateTime.now());
        
        mockTransactionDatabase.put(referenceCode, transaction);
        
        log.info("✅ Simulated transaction created: {} for reference: {}", transactionId, referenceCode);
        
        // Publish Redis event for PaymentEventListener to handle
        try {
            // Find the actual payment to get correct userId and tenantId
            SimplePayment payment = paymentRepository.findByReferenceCode(referenceCode).orElse(null);
            
            if (payment != null) {
                // Create PaymentEvent with actual payment data
                var event = redisPaymentService.createPaymentEvent(
                    referenceCode, 
                    payment.getUserId(), // actual userId
                    payment.getTenantId(), // actual tenantId
                    amount.toString(),
                    "VND",
                    "Simulated bank transaction"
                );
                event.setType("PAYMENT_SIMULATED");
                event.setBankTransactionId(transactionId);
                event.setUpdatedAt(LocalDateTime.now());
                
                log.info("🧪 Publishing simulated payment event for tenant: {}", payment.getTenantId());
                
                // Use RedisPaymentService to publish
                redisPaymentService.publishPaymentEvent(event);
            } else {
                log.warn("⚠️ Payment not found for reference: {}, skipping event publish", referenceCode);
            }
            
            log.info("✅ Published simulated payment event: {}", referenceCode);
        } catch (Exception e) {
            log.error("❌ Failed to publish simulated payment event: {}", e.getMessage(), e);
        }
    }

    private BigDecimal getMockAmount(String referenceCode) {
        // Generate mock amount based on reference code
        int hashCode = referenceCode.hashCode();
        return new BigDecimal(50000 + (Math.abs(hashCode) % 500000)); // 50k - 550k VND
    }

    /**
     * Mock bank transaction class
     */
    public static class BankTransaction {
        private final String transactionId;
        private final String referenceCode;
        private final BigDecimal amount;
        private final LocalDateTime timestamp;
        private boolean processed = false;

        public BankTransaction(String transactionId, String referenceCode, BigDecimal amount, LocalDateTime timestamp) {
            this.transactionId = transactionId;
            this.referenceCode = referenceCode;
            this.amount = amount;
            this.timestamp = timestamp;
        }

        // Getters
        public String getTransactionId() { return transactionId; }
        public String getReferenceCode() { return referenceCode; }
        public BigDecimal getAmount() { return amount; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isProcessed() { return processed; }
        public void setProcessed(boolean processed) { this.processed = processed; }
    }
}
