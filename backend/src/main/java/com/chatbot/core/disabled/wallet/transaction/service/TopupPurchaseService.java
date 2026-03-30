package com.chatbot.core.wallet.transaction.service;

import com.chatbot.core.wallet.transaction.dto.TopupRequest;
import com.chatbot.core.wallet.transaction.dto.PurchaseRequest;
import com.chatbot.core.wallet.transaction.dto.TransactionResponse;
import com.chatbot.core.wallet.transaction.model.TransactionType;
import com.chatbot.core.wallet.wallet.service.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopupPurchaseService {

    private final TransactionService transactionService;
    private final BalanceService balanceService;

    @Transactional
    public TransactionResponse processTopup(TopupRequest request) {
        log.info("Processing topup for wallet {}: {}", request.getWalletId(), request.getAmount());

        // Create transaction
        TransactionResponse transaction = transactionService.createTransaction(
                request.getWalletId(),
                TransactionType.TOPUP,
                request.getAmount(),
                "USD", // Default currency, should be fetched from wallet
                request.getDescription() != null ? request.getDescription() : "Wallet topup",
                request.getExternalReference()
        );

        try {
            // Complete the transaction (credit the balance)
            TransactionResponse completedTransaction = transactionService.completeTransaction(transaction.getId());
            
            log.info("Successfully processed topup: {}", completedTransaction.getTransactionReference());
            return completedTransaction;
        } catch (Exception e) {
            log.error("Failed to process topup for transaction: {}", transaction.getTransactionReference(), e);
            transactionService.failTransaction(transaction.getId(), e.getMessage());
            throw new RuntimeException("Topup processing failed", e);
        }
    }

    @Transactional
    public TransactionResponse processPurchase(PurchaseRequest request) {
        log.info("Processing purchase for wallet {}: {}", request.getWalletId(), request.getAmount());

        // Check if wallet has sufficient balance
        if (!balanceService.hasSufficientBalance(request.getWalletId(), request.getAmount())) {
            throw new IllegalStateException("Insufficient balance for purchase");
        }

        // Create transaction
        TransactionResponse transaction = transactionService.createTransaction(
                request.getWalletId(),
                TransactionType.PURCHASE,
                request.getAmount(),
                "USD", // Default currency, should be fetched from wallet
                request.getDescription() != null ? request.getDescription() : "Purchase",
                request.getExternalReference()
        );

        try {
            // Complete the transaction (debit the balance)
            TransactionResponse completedTransaction = transactionService.completeTransaction(transaction.getId());
            
            log.info("Successfully processed purchase: {}", completedTransaction.getTransactionReference());
            return completedTransaction;
        } catch (Exception e) {
            log.error("Failed to process purchase for transaction: {}", transaction.getTransactionReference(), e);
            transactionService.failTransaction(transaction.getId(), e.getMessage());
            throw new RuntimeException("Purchase processing failed", e);
        }
    }

    @Transactional
    public TransactionResponse processRefund(Long originalTransactionId, String reason) {
        log.info("Processing refund for transaction: {}", originalTransactionId);

        // Get original transaction
        TransactionResponse originalTransaction = transactionService.getTransactionByReference(
                originalTransactionId.toString());

        if (!"COMPLETED".equals(originalTransaction.getStatus())) {
            throw new IllegalStateException("Cannot refund incomplete transaction");
        }

        // Create refund transaction (negative amount)
        TransactionResponse refundTransaction = transactionService.createTransaction(
                originalTransaction.getWalletId(),
                TransactionType.REFUND,
                originalTransaction.getAmount().negate(),
                originalTransaction.getCurrency(),
                "Refund: " + reason,
                originalTransaction.getTransactionReference()
        );

        try {
            // Complete the refund transaction
            TransactionResponse completedRefund = transactionService.completeTransaction(refundTransaction.getId());
            
            log.info("Successfully processed refund: {}", completedRefund.getTransactionReference());
            return completedRefund;
        } catch (Exception e) {
            log.error("Failed to process refund for transaction: {}", refundTransaction.getTransactionReference(), e);
            transactionService.failTransaction(refundTransaction.getId(), e.getMessage());
            throw new RuntimeException("Refund processing failed", e);
        }
    }
}
