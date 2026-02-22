package com.chatbot.core.wallet.ledger.repository;

import com.chatbot.core.wallet.ledger.model.Ledger;
import com.chatbot.core.wallet.ledger.model.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<Ledger, Long> {
    
    List<Ledger> findByTransactionIdOrderByCreatedAt(Long transactionId);
    
    List<Ledger> findByBatchReferenceOrderByCreatedAt(String batchReference);
    
    List<Ledger> findByAccountCodeOrderByCreatedAtDesc(String accountCode);
    
    List<Ledger> findByAccountTypeOrderByCreatedAtDesc(AccountType accountType);
    
    @Query("SELECT l FROM Ledger l WHERE l.accountCode = :accountCode AND l.createdAt >= :startDate AND l.createdAt <= :endDate ORDER BY l.createdAt DESC")
    List<Ledger> findByAccountCodeAndDateRange(@Param("accountCode") String accountCode, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(l.debitAmount) FROM Ledger l WHERE l.accountCode = :accountCode AND l.debitAmount IS NOT NULL")
    BigDecimal sumDebitsByAccountCode(@Param("accountCode") String accountCode);
    
    @Query("SELECT SUM(l.creditAmount) FROM Ledger l WHERE l.accountCode = :accountCode AND l.creditAmount IS NOT NULL")
    BigDecimal sumCreditsByAccountCode(@Param("accountCode") String accountCode);
    
    @Query("SELECT l FROM Ledger l WHERE l.transactionId = :transactionId ORDER BY l.createdAt ASC")
    List<Ledger> findLedgerEntriesByTransactionId(@Param("transactionId") Long transactionId);
    
    @Query("SELECT COUNT(l) FROM Ledger l WHERE l.batchReference = :batchReference")
    long countLedgerEntriesByBatchReference(@Param("batchReference") String batchReference);
}
