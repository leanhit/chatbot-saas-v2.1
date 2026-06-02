package com.chatbot.core.simplepayment.service;

import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService {

    private final UserRepository userRepository;

    /**
     * Deduct user balance in a separate transaction using userTransactionManager.
     * This method ensures a transaction is active for the user DB.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, transactionManager = "userTransactionManager")
    public void deductUserBalance(Long userId, BigDecimal amount) {
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Initialize balance if null
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }

        // Check sufficient balance
        if (user.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException(String.format("Insufficient balance. Required: %s, Available: %s", amount, user.getBalance()));
        }

        BigDecimal oldBalance = user.getBalance();
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);

        log.info("💸 Deducted balance for user {}: {} - {} = {}", userId, oldBalance, amount, user.getBalance());
    }
    /**
    * Credit user balance in a separate transaction using userTransactionManager.
    * This adds amount to the user's balance.
    */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, transactionManager = "userTransactionManager")
    public void creditUserBalance(Long userId, BigDecimal amount) {
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Initialize balance if null
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }

        BigDecimal oldBalance = user.getBalance();
        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        log.info("💸 Credited balance for user {}: {} + {} = {}", userId, oldBalance, amount, user.getBalance());
    }
}

