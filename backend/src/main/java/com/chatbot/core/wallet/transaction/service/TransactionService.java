package com.chatbot.core.wallet.transaction.service;

import com.chatbot.core.wallet.transaction.dto.TransactionResponse;
import com.chatbot.core.wallet.transaction.model.Transaction;
import com.chatbot.core.wallet.transaction.model.TransactionStatus;
import com.chatbot.core.wallet.transaction.model.TransactionType;
import com.chatbot.core.wallet.transaction.repository.TransactionRepository;
import com.chatbot.core.wallet.wallet.service.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BalanceService balanceService;

    public TransactionResponse createTransaction(Long walletId, TransactionType type, 
                                             java.math.BigDecimal amount, String currency, 
                                             String description, String externalReference) {
        
        Transaction transaction = Transaction.builder()
                .walletId(walletId)
                .transactionType(type)
                .amount(amount)
                .currency(currency)
                .description(description)
                .externalReference(externalReference)
                .status(TransactionStatus.PENDING)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Created transaction: {} for wallet: {}", savedTransaction.getTransactionReference(), walletId);
        
        return toTransactionResponse(savedTransaction);
    }

    public TransactionResponse completeTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not in pending state");
        }

        // Update wallet balance based on transaction type
        switch (transaction.getTransactionType()) {
            case TOPUP:
            case TRANSFER_IN:
            case REFUND:
            case REWARD:
                balanceService.creditBalance(transaction.getWalletId(), transaction.getAmount());
                break;
            case PURCHASE:
            case TRANSFER_OUT:
            case FEE:
                balanceService.debitBalance(transaction.getWalletId(), transaction.getAmount());
                break;
            case ADJUSTMENT:
                // Adjustments can be either credit or debit based on amount sign
                if (transaction.getAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    balanceService.creditBalance(transaction.getWalletId(), transaction.getAmount());
                } else {
                    balanceService.debitBalance(transaction.getWalletId(), transaction.getAmount().abs());
                }
                break;
        }

        transaction.setStatus(TransactionStatus.COMPLETED);
        Transaction completedTransaction = transactionRepository.save(transaction);
        
        log.info("Completed transaction: {}", completedTransaction.getTransactionReference());
        return toTransactionResponse(completedTransaction);
    }

    public TransactionResponse failTransaction(Long transactionId, String reason) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setStatus(TransactionStatus.FAILED);
        if (transaction.getDescription() == null) {
            transaction.setDescription(reason);
        } else {
            transaction.setDescription(transaction.getDescription() + " | Failed: " + reason);
        }

        Transaction failedTransaction = transactionRepository.save(transaction);
        log.warn("Failed transaction: {} - Reason: {}", failedTransaction.getTransactionReference(), reason);
        
        return toTransactionResponse(failedTransaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(Long walletId) {
        List<Transaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
        return transactions.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(Long walletId, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable);
        return transactions.map(this::toTransactionResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String reference) {
        Transaction transaction = transactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return toTransactionResponse(transaction);
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setWalletId(transaction.getWalletId());
        response.setTransactionReference(transaction.getTransactionReference());
        response.setTransactionType(transaction.getTransactionType().name());
        response.setAmount(transaction.getAmount());
        response.setCurrency(transaction.getCurrency());
        response.setDescription(transaction.getDescription());
        response.setStatus(transaction.getStatus().name());
        response.setExternalReference(transaction.getExternalReference());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        return response;
    }
}
