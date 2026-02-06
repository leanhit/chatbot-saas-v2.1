package com.chatbot.modules.wallet.core.repository;

import com.chatbot.modules.wallet.core.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Wallet repository for v0.1
 * Wallet data access operations
 */
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    /**
     * Find wallet by tenant ID
     */
    Optional<Wallet> findByTenantId(UUID tenantId);

    /**
     * Check if wallet exists for tenant
     */
    boolean existsByTenantId(UUID tenantId);
}
