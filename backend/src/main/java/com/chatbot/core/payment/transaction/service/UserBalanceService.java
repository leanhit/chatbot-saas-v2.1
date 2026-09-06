package com.chatbot.core.payment.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBalanceService {

    /**
     * Update user balance in a separate transaction
     * This will be implemented after user service integration
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, transactionManager = "userTransactionManager")
    public void updateUserBalanceInSeparateTransaction(Long userId, BigDecimal amount) {
        log.info("💰 Updating balance for user: {}, amount: {}", userId, amount);
        // Implementation will be added after user service migration
    }

    /**
     * Deduct user balance
     */
    @Transactional(transactionManager = "userTransactionManager")
    public void deductUserBalance(Long userId, BigDecimal amount) {
        log.info("💸 Deducting balance for user: {}, amount: {}", userId, amount);
        // Implementation will be added after user service migration
    }

    /**
     * Check if user has sufficient balance
     */
    @Transactional(readOnly = true, transactionManager = "userTransactionManager")
    public boolean hasSufficientBalance(Long userId, BigDecimal requiredAmount) {
        log.debug("Checking balance for user: {}, required: {}", userId, requiredAmount);
        // Implementation will be added after user service migration
        return true; // Placeholder
    }

    /**
     * Credit user balance
     */
    @Transactional(transactionManager = "userTransactionManager")
    public void creditUserBalance(Long userId, BigDecimal amount) {
        log.info("💰 Crediting balance for user: {}, amount: {}", userId, amount);
        // Implementation will be added after user service migration
    }
}
