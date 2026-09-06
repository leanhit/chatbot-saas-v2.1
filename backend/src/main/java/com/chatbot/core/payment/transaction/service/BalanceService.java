package com.chatbot.core.payment.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService {

    private final UserBalanceService userBalanceService;

    /**
     * Credit user balance
     */
    @Transactional(transactionManager = "userTransactionManager")
    public void creditUserBalance(Long userId, BigDecimal amount) {
        log.info("💰 Crediting user balance via BalanceService: {}, amount: {}", userId, amount);
        userBalanceService.creditUserBalance(userId, amount);
    }

    /**
     * Deduct user balance
     */
    @Transactional(transactionManager = "userTransactionManager")
    public void deductUserBalance(Long userId, BigDecimal amount) {
        log.info("💸 Deducting user balance via BalanceService: {}, amount: {}", userId, amount);
        userBalanceService.deductUserBalance(userId, amount);
    }

    /**
     * Check sufficient balance
     */
    @Transactional(readOnly = true, transactionManager = "userTransactionManager")
    public boolean hasSufficientBalance(Long userId, BigDecimal requiredAmount) {
        return userBalanceService.hasSufficientBalance(userId, requiredAmount);
    }
}
