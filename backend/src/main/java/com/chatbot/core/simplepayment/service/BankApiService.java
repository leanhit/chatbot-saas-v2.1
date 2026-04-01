package com.chatbot.core.simplepayment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class BankApiService {

    // Mock database for demo - in real implementation, call actual bank API
    private final Map<String, BankTransaction> mockTransactionDatabase = new ConcurrentHashMap<>();
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private RedisPaymentService redisPaymentService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Find transaction by reference code
     * In real implementation, this would call bank's API
     */
    public String findTransactionByReference(String referenceCode) {
        log.info("🏦 Checking bank API for reference: {}", referenceCode);

        try {
            // Simulate API call delay
            Thread.sleep(500);

            // Mock implementation - check if transaction exists
            BankTransaction transaction = mockTransactionDatabase.get(referenceCode);
            
            if (transaction != null && !transaction.isProcessed()) {
                // Mark as processed to avoid duplicate processing
                transaction.setProcessed(true);
                log.info("✅ Found bank transaction: {} for reference: {}", transaction.getTransactionId(), referenceCode);
                return transaction.getTransactionId();
            }

            // Simulate random transaction found (for demo)
            if (Math.random() > 0.7) { // 30% chance to find transaction
                String transactionId = "BANK" + System.currentTimeMillis();
                BankTransaction newTransaction = new BankTransaction(
                    transactionId, 
                    referenceCode, 
                    getMockAmount(referenceCode),
                    LocalDateTime.now()
                );
                mockTransactionDatabase.put(referenceCode, newTransaction);
                
                log.info("🎲 Simulated bank transaction found: {} for reference: {}", transactionId, referenceCode);
                return transactionId;
            }

            log.info("📭 No transaction found for reference: {}", referenceCode);
            return null;

        } catch (Exception e) {
            log.error("❌ Error checking bank API: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get recent transactions from bank
     * In real implementation, this would call bank's API
     */
    public List<BankTransaction> getRecentTransactions() {
        log.info("🏦 Fetching recent transactions from bank");

        try {
            // Simulate API call
            Thread.sleep(1000);

            // Return mock transactions
            List<BankTransaction> transactions = new ArrayList<>();
            
            // Add some mock transactions for demo
            for (int i = 0; i < 5; i++) {
                String referenceCode = "NAP" + (1000 + i);
                if (Math.random() > 0.5) {
                    transactions.add(new BankTransaction(
                        "BANK" + System.currentTimeMillis() + i,
                        referenceCode,
                        new BigDecimal(100000 * (i + 1)),
                        LocalDateTime.now().minusMinutes(i * 10)
                    ));
                }
            }

            log.info("✅ Found {} recent transactions", transactions.size());
            return transactions;

        } catch (Exception e) {
            log.error("❌ Error fetching recent transactions: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Validate transaction with bank
     * In real implementation, this would validate transaction details
     */
    public boolean validateTransaction(String transactionId, String referenceCode, BigDecimal amount) {
        log.info("🔍 Validating transaction: {} for reference: {} amount: {}", transactionId, referenceCode, amount);

        try {
            // Simulate validation
            Thread.sleep(300);

            // Mock validation - always return true for demo
            boolean isValid = Math.random() > 0.1; // 90% success rate

            log.info("✅ Transaction validation result: {}", isValid);
            return isValid;

        } catch (Exception e) {
            log.error("❌ Error validating transaction: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get bank balance
     * In real implementation, this would get actual bank balance
     */
    public BigDecimal getBankBalance() {
        log.info("💰 Checking bank balance");

        try {
            // Simulate API call
            Thread.sleep(500);

            // Mock balance
            BigDecimal balance = new BigDecimal("500000000"); // 500 million VND

            log.info("✅ Bank balance: {}", balance);
            return balance;

        } catch (Exception e) {
            log.error("❌ Error checking bank balance: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
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
            // Create PaymentEvent with proper structure using RedisPaymentService
            var event = redisPaymentService.createPaymentEvent(
                referenceCode, 
                1L, // userId
                1L, // tenantId  
                amount.toString(),
                "VND",
                "Simulated bank transaction"
            );
            event.setType("PAYMENT_SIMULATED");
            event.setBankTransactionId(transactionId);
            event.setUpdatedAt(LocalDateTime.now());
            
            // Use RedisPaymentService to publish
            redisPaymentService.publishPaymentEvent(event);
            
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
