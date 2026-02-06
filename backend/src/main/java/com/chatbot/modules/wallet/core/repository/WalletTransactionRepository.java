package com.chatbot.modules.wallet.core.repository;

import com.chatbot.modules.wallet.core.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * WalletTransaction repository for v0.1
 * Transaction data access operations
 */
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {

    /**
     * Find transactions by wallet ID
     */
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId);

    /**
     * Find transactions by wallet ID with pagination
     */
    @Query("SELECT t FROM WalletTransaction t WHERE t.wallet.id = :walletId ORDER BY t.createdAt DESC")
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(UUID walletId, org.springframework.data.domain.Pageable pageable);
}
