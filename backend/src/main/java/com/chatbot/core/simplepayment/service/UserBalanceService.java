package com.chatbot.core.simplepayment.service;

import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Handles user balance updates in a dedicated transaction using the userTransactionManager.
 * This class is separate from SimplePaymentService to avoid self‑invocation issues where
 * Spring @Transactional annotations would be ignored.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserBalanceService {

    private final UserRepository userRepository;

    /**
     * Update the balance of the given user. Executes in a REQUIRES_NEW transaction using the
     * dedicated user data source/transaction manager.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "userTransactionManager")
    public void updateUserBalanceInSeparateTransaction(Long userId, BigDecimal amount) {
        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Initialise balance if null
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }

        user.setBalance(user.getBalance().add(amount));
        userRepository.save(user);

        log.info("💸 Updated user balance: {} + {} = {}", userId, amount, user.getBalance());
    }
}
