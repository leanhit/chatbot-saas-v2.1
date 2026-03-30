package com.chatbot.core.wallet.ledger.service;

import com.chatbot.core.wallet.ledger.model.AccountType;
import com.chatbot.core.wallet.ledger.model.LedgerEntry;
import com.chatbot.core.wallet.transaction.model.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoubleEntryService {

    private final LedgerService ledgerService;

    public void recordTopupTransaction(Long transactionId, Long walletId, BigDecimal amount, String currency) {
        List<LedgerEntry> entries = new ArrayList<>();

        // Debit: Cash/Bank Account (increase in assets)
        entries.add(LedgerEntry.debit(
                "CASH_" + currency,
                "Cash/Bank Account",
                amount,
                currency,
                "Wallet topup - Transaction ID: " + transactionId
        ));

        // Credit: User Wallet Liability (increase in liabilities)
        entries.add(LedgerEntry.credit(
                "WALLET_" + walletId,
                "User Wallet Liability",
                amount,
                currency,
                "Wallet topup - Transaction ID: " + transactionId
        ));

        ledgerService.recordLedgerEntries(transactionId, entries);
        log.info("Recorded double-entry for topup transaction: {}", transactionId);
    }

    public void recordPurchaseTransaction(Long transactionId, Long walletId, BigDecimal amount, String currency) {
        List<LedgerEntry> entries = new ArrayList<>();

        // Debit: User Wallet Liability (decrease in liabilities)
        entries.add(LedgerEntry.debit(
                "WALLET_" + walletId,
                "User Wallet Liability",
                amount,
                currency,
                "Purchase - Transaction ID: " + transactionId
        ));

        // Credit: Revenue Account (increase in revenue)
        entries.add(LedgerEntry.credit(
                "REVENUE_PURCHASE",
                "Purchase Revenue",
                amount,
                currency,
                "Purchase - Transaction ID: " + transactionId
        ));

        ledgerService.recordLedgerEntries(transactionId, entries);
        log.info("Recorded double-entry for purchase transaction: {}", transactionId);
    }

    public void recordTransferOutTransaction(Long transactionId, Long walletId, BigDecimal amount, String currency) {
        List<LedgerEntry> entries = new ArrayList<>();

        // Debit: User Wallet Liability (decrease in liabilities)
        entries.add(LedgerEntry.debit(
                "WALLET_" + walletId,
                "User Wallet Liability",
                amount,
                currency,
                "Transfer out - Transaction ID: " + transactionId
        ));

        // Credit: Cash/Bank Account (decrease in assets)
        entries.add(LedgerEntry.credit(
                "CASH_" + currency,
                "Cash/Bank Account",
                amount,
                currency,
                "Transfer out - Transaction ID: " + transactionId
        ));

        ledgerService.recordLedgerEntries(transactionId, entries);
        log.info("Recorded double-entry for transfer out transaction: {}", transactionId);
    }

    public void recordTransferInTransaction(Long transactionId, Long walletId, BigDecimal amount, String currency) {
        List<LedgerEntry> entries = new ArrayList<>();

        // Debit: Cash/Bank Account (increase in assets)
        entries.add(LedgerEntry.debit(
                "CASH_" + currency,
                "Cash/Bank Account",
                amount,
                currency,
                "Transfer in - Transaction ID: " + transactionId
        ));

        // Credit: User Wallet Liability (increase in liabilities)
        entries.add(LedgerEntry.credit(
                "WALLET_" + walletId,
                "User Wallet Liability",
                amount,
                currency,
                "Transfer in - Transaction ID: " + transactionId
        ));

        ledgerService.recordLedgerEntries(transactionId, entries);
        log.info("Recorded double-entry for transfer in transaction: {}", transactionId);
    }

    public void recordRefundTransaction(Long transactionId, Long walletId, BigDecimal amount, String currency) {
        List<LedgerEntry> entries = new ArrayList<>();

        // Debit: Revenue Account (decrease in revenue)
        entries.add(LedgerEntry.debit(
                "REVENUE_PURCHASE",
                "Purchase Revenue",
                amount,
                currency,
                "Refund - Transaction ID: " + transactionId
        ));

        // Credit: User Wallet Liability (increase in liabilities)
        entries.add(LedgerEntry.credit(
                "WALLET_" + walletId,
                "User Wallet Liability",
                amount,
                currency,
                "Refund - Transaction ID: " + transactionId
        ));

        ledgerService.recordLedgerEntries(transactionId, entries);
        log.info("Recorded double-entry for refund transaction: {}", transactionId);
    }

    public void recordFeeTransaction(Long transactionId, BigDecimal amount, String currency) {
        List<LedgerEntry> entries = new ArrayList<>();

        // Debit: Cash/Bank Account (increase in assets)
        entries.add(LedgerEntry.debit(
                "CASH_" + currency,
                "Cash/Bank Account",
                amount,
                currency,
                "Fee collected - Transaction ID: " + transactionId
        ));

        // Credit: Fee Revenue Account (increase in revenue)
        entries.add(LedgerEntry.credit(
                "REVENUE_FEES",
                "Fee Revenue",
                amount,
                currency,
                "Fee collected - Transaction ID: " + transactionId
        ));

        ledgerService.recordLedgerEntries(transactionId, entries);
        log.info("Recorded double-entry for fee transaction: {}", transactionId);
    }

    public void recordAdjustmentTransaction(Long transactionId, String accountCode, BigDecimal amount, String currency, String description) {
        List<LedgerEntry> entries = new ArrayList<>();

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            // Positive adjustment: Debit Cash, Credit Adjustment Equity
            entries.add(LedgerEntry.debit(
                    "CASH_" + currency,
                    "Cash/Bank Account",
                    amount,
                    currency,
                    description
            ));
            entries.add(LedgerEntry.credit(
                    "EQUITY_ADJUSTMENT",
                    "Adjustment Equity",
                    amount,
                    currency,
                    description
            ));
        } else {
            // Negative adjustment: Debit Adjustment Equity, Credit Cash
            entries.add(LedgerEntry.debit(
                    "EQUITY_ADJUSTMENT",
                    "Adjustment Equity",
                    amount.abs(),
                    currency,
                    description
            ));
            entries.add(LedgerEntry.credit(
                    "CASH_" + currency,
                    "Cash/Bank Account",
                    amount.abs(),
                    currency,
                    description
            ));
        }

        ledgerService.recordLedgerEntries(transactionId, entries);
        log.info("Recorded double-entry for adjustment transaction: {}", transactionId);
    }
}
