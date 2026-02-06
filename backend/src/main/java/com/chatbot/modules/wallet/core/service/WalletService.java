package com.chatbot.modules.wallet.core.service;

import com.chatbot.modules.wallet.core.dto.WalletResponse;
import com.chatbot.modules.wallet.core.dto.WalletTransactionRequest;
import com.chatbot.modules.wallet.core.dto.WalletTransactionResponse;
import com.chatbot.modules.wallet.core.model.Wallet;
import com.chatbot.modules.wallet.core.model.WalletTransaction;
import com.chatbot.modules.wallet.core.repository.WalletRepository;
import com.chatbot.modules.wallet.core.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Wallet service for v0.1
 * Ledger-based wallet management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    /**
     * Get wallet information
     * Auto-creates wallet if not exists
     * TODO: Validate tenant exists and is ACTIVE
     */
    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID tenantId) {
        // TODO: Validate tenant exists and is ACTIVE via Tenant Hub API
        // TODO: Validate user has permission to access wallet
        
        Wallet wallet = walletRepository
                .findByTenantId(tenantId)
                .orElseGet(() -> createWallet(tenantId));
        
        return mapToResponse(wallet);
    }

    /**
     * Get wallet transaction history
     * Returns empty list if wallet doesn't exist yet
     * TODO: Validate tenant exists and is ACTIVE
     */
    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getTransactions(UUID tenantId) {
        // TODO: Validate tenant exists and is ACTIVE via Tenant Hub API
        // TODO: Validate user has permission to access wallet
        
        Wallet wallet = walletRepository
                .findByTenantId(tenantId)
                .orElse(null);
        
        if (wallet == null) {
            return List.of();
        }
        
        List<WalletTransaction> transactions = transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId());
        
        return transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Add money to wallet (TOPUP)
     * Creates wallet if not exists
     * TODO: Validate tenant exists and is ACTIVE
     */
    @Transactional
    public WalletResponse topup(UUID tenantId, WalletTransactionRequest request) {
        // TODO: Validate tenant exists and is ACTIVE via Tenant Hub API
        // TODO: Validate user has permission to modify wallet
        
        validateAmount(request.getAmount());
        
        Wallet wallet = walletRepository
                .findByTenantId(tenantId)
                .orElseGet(() -> createWallet(tenantId));
        
        // Update balance
        wallet.addBalance(request.getAmount());
        wallet = walletRepository.save(wallet);
        
        // Create transaction record
        createTransaction(wallet, WalletTransaction.TransactionType.TOPUP, request.getAmount(), request.getReference());
        
        log.info("Topped up wallet: tenant={}, amount={}, new balance={}", 
                tenantId, request.getAmount(), wallet.getBalance());
        
        return mapToResponse(wallet);
    }

    /**
     * Deduct money from wallet (PURCHASE)
     * Fails if insufficient balance
     * TODO: Validate tenant exists and is ACTIVE
     */
    @Transactional
    public WalletResponse deduct(UUID tenantId, WalletTransactionRequest request) {
        // TODO: Validate tenant exists and is ACTIVE via Tenant Hub API
        // TODO: Validate user has permission to modify wallet
        
        validateAmount(request.getAmount());
        
        Wallet wallet = walletRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for tenant: " + tenantId));
        
        // Check sufficient balance
        if (!wallet.hasSufficientBalance(request.getAmount())) {
            throw new IllegalStateException("Insufficient balance. Current: " + wallet.getBalance() + ", Required: " + request.getAmount());
        }
        
        // Update balance
        wallet.deductBalance(request.getAmount());
        wallet = walletRepository.save(wallet);
        
        // Create transaction record
        createTransaction(wallet, WalletTransaction.TransactionType.PURCHASE, request.getAmount(), request.getReference());
        
        log.info("Deducted from wallet: tenant={}, amount={}, new balance={}", 
                tenantId, request.getAmount(), wallet.getBalance());
        
        return mapToResponse(wallet);
    }

    /**
     * Create new wallet with zero balance
     */
    private Wallet createWallet(UUID tenantId) {
        Wallet wallet = Wallet.builder()
                .tenantId(tenantId)
                .balance(BigDecimal.ZERO)
                .build();
        
        wallet = walletRepository.save(wallet);
        log.info("Created new wallet for tenant: {}", tenantId);
        
        return wallet;
    }

    /**
     * Create immutable transaction record
     */
    private void createTransaction(Wallet wallet, WalletTransaction.TransactionType type, BigDecimal amount, String reference) {
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .reference(reference)
                .build();
        
        transactionRepository.save(transaction);
    }

    /**
     * Validate amount is positive
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
    }

    private WalletResponse mapToResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .tenantId(wallet.getTenantId())
                .balance(wallet.getBalance())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private WalletTransactionResponse mapToResponse(WalletTransaction transaction) {
        return WalletTransactionResponse.builder()
                .id(transaction.getId())
                .walletId(transaction.getWallet().getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .reference(transaction.getReference())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
