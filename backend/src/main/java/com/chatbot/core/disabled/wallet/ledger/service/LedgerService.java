package com.chatbot.core.wallet.ledger.service;

import com.chatbot.core.wallet.ledger.model.Ledger;
import com.chatbot.core.wallet.ledger.model.LedgerEntry;
import com.chatbot.core.wallet.ledger.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final LedgerRepository ledgerRepository;

    @Transactional
    public void recordLedgerEntries(Long transactionId, List<LedgerEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("Ledger entries cannot be null or empty");
        }

        // Validate double-entry principle: total debits must equal total credits
        BigDecimal totalDebits = entries.stream()
                .filter(entry -> entry.getDebitAmount() != null)
                .map(LedgerEntry::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = entries.stream()
                .filter(entry -> entry.getCreditAmount() != null)
                .map(LedgerEntry::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new IllegalArgumentException("Total debits must equal total credits for double-entry bookkeeping");
        }

        String batchReference = generateBatchReference();

        for (LedgerEntry entry : entries) {
            Ledger ledger = Ledger.builder()
                    .transactionId(transactionId)
                    .batchReference(batchReference)
                    .accountType(entry.getAccountType())
                    .accountCode(entry.getAccountCode())
                    .accountName(entry.getAccountName())
                    .debitAmount(entry.getDebitAmount())
                    .creditAmount(entry.getCreditAmount())
                    .currency(entry.getCurrency())
                    .description(entry.getDescription())
                    .build();

            ledgerRepository.save(ledger);
        }

        log.info("Recorded {} ledger entries for transaction {} with batch reference {}", 
                entries.size(), transactionId, batchReference);
    }

    @Transactional(readOnly = true)
    public List<Ledger> getLedgerEntriesByTransactionId(Long transactionId) {
        return ledgerRepository.findLedgerEntriesByTransactionId(transactionId);
    }

    @Transactional(readOnly = true)
    public List<Ledger> getLedgerEntriesByBatchReference(String batchReference) {
        return ledgerRepository.findByBatchReferenceOrderByCreatedAt(batchReference);
    }

    @Transactional(readOnly = true)
    public BigDecimal getAccountBalance(String accountCode) {
        BigDecimal totalDebits = ledgerRepository.sumDebitsByAccountCode(accountCode);
        BigDecimal totalCredits = ledgerRepository.sumCreditsByAccountCode(accountCode);
        
        if (totalDebits == null) totalDebits = BigDecimal.ZERO;
        if (totalCredits == null) totalCredits = BigDecimal.ZERO;
        
        return totalDebits.subtract(totalCredits);
    }

    @Transactional(readOnly = true)
    public boolean validateDoubleEntryForTransaction(Long transactionId) {
        List<Ledger> entries = ledgerRepository.findLedgerEntriesByTransactionId(transactionId);
        
        BigDecimal totalDebits = entries.stream()
                .filter(entry -> entry.getDebitAmount() != null)
                .map(Ledger::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredits = entries.stream()
                .filter(entry -> entry.getCreditAmount() != null)
                .map(Ledger::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalDebits.compareTo(totalCredits) == 0;
    }

    private String generateBatchReference() {
        return "BATCH" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
}
