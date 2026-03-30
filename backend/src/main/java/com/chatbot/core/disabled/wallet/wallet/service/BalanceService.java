package com.chatbot.core.wallet.wallet.service;

import com.chatbot.core.wallet.wallet.model.Wallet;
import com.chatbot.core.wallet.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService {

    private final WalletRepository walletRepository;

    @Transactional
    public void creditBalance(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        if (!wallet.getIsActive()) {
            throw new IllegalStateException("Wallet is not active");
        }
        
        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        
        log.info("Credited {} to wallet {}. New balance: {}", amount, walletId, newBalance);
    }

    @Transactional
    public void debitBalance(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        if (!wallet.getIsActive()) {
            throw new IllegalStateException("Wallet is not active");
        }
        
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        
        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        
        log.info("Debited {} from wallet {}. New balance: {}", amount, walletId, newBalance);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCurrentBalance(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        return wallet.getBalance();
    }

    @Transactional(readOnly = true)
    public Optional<Wallet> findWalletByUserAndCurrency(Long userId, Long tenantId, String currency) {
        return walletRepository.findByUserIdAndTenantIdAndCurrency(userId, tenantId, currency);
    }

    @Transactional
    public boolean hasSufficientBalance(Long walletId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        return wallet.getIsActive() && wallet.getBalance().compareTo(amount) >= 0;
    }
}
