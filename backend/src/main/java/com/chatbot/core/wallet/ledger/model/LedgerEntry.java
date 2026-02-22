package com.chatbot.core.wallet.ledger.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {
    
    private AccountType accountType;
    private String accountCode;
    private String accountName;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String currency;
    private String description;
    
    public static LedgerEntry debit(String accountCode, String accountName, BigDecimal amount, String currency, String description) {
        return LedgerEntry.builder()
                .accountType(AccountType.ASSET)
                .accountCode(accountCode)
                .accountName(accountName)
                .debitAmount(amount)
                .creditAmount(BigDecimal.ZERO)
                .currency(currency)
                .description(description)
                .build();
    }
    
    public static LedgerEntry credit(String accountCode, String accountName, BigDecimal amount, String currency, String description) {
        return LedgerEntry.builder()
                .accountType(AccountType.ASSET)
                .accountCode(accountCode)
                .accountName(accountName)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(amount)
                .currency(currency)
                .description(description)
                .build();
    }
}
