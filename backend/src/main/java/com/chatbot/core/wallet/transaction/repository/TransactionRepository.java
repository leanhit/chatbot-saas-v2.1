package com.chatbot.core.wallet.transaction.repository;

import com.chatbot.core.wallet.transaction.model.Transaction;
import com.chatbot.core.wallet.transaction.model.TransactionStatus;
import com.chatbot.core.wallet.transaction.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionReference(String transactionReference);
    
    List<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    
    Page<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
    
    List<Transaction> findByWalletIdAndStatusOrderByCreatedAtDesc(Long walletId, TransactionStatus status);
    
    List<Transaction> findByWalletIdAndTransactionTypeOrderByCreatedAtDesc(Long walletId, TransactionType transactionType);
    
    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId AND t.createdAt >= :startDate AND t.createdAt <= :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletIdAndDateRange(@Param("walletId") Long walletId, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.walletId = :walletId AND t.status = :status")
    long countTransactionsByWalletIdAndStatus(@Param("walletId") Long walletId, @Param("status") TransactionStatus status);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.walletId = :walletId AND t.transactionType = :type AND t.status = 'COMPLETED'")
    java.math.BigDecimal sumCompletedTransactionsByType(@Param("walletId") Long walletId, @Param("type") TransactionType type);
}
