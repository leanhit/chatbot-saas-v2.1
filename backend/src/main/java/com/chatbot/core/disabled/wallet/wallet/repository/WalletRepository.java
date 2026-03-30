package com.chatbot.core.wallet.wallet.repository;

import com.chatbot.core.wallet.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    Optional<Wallet> findByUserIdAndTenantIdAndCurrency(Long userId, Long tenantId, String currency);
    
    List<Wallet> findByUserIdAndTenantId(Long userId, Long tenantId);
    
    List<Wallet> findByUserIdAndIsActiveTrue(Long userId);
    
    List<Wallet> findByTenantIdAndIsActiveTrue(Long tenantId);
    
    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId AND w.tenantId = :tenantId AND w.isActive = true")
    List<Wallet> findActiveWalletsByUserAndTenant(@Param("userId") Long userId, @Param("tenantId") Long tenantId);
    
    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.userId = :userId AND w.tenantId = :tenantId AND w.isActive = true")
    long countActiveWalletsByUserAndTenant(@Param("userId") Long userId, @Param("tenantId") Long tenantId);
}
