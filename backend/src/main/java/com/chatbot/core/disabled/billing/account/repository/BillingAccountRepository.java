package com.chatbot.core.billing.account.repository;

import com.chatbot.core.billing.account.model.BillingAccount;
import com.chatbot.core.billing.model.BillingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingAccountRepository extends JpaRepository<BillingAccount, Long> {

    /**
     * Find billing account by tenant ID
     */
    Optional<BillingAccount> findByTenantId(Long tenantId);

    /**
     * Find billing account by account number
     */
    Optional<BillingAccount> findByAccountNumber(String accountNumber);

    /**
     * Check if billing account exists for tenant
     */
    boolean existsByTenantId(Long tenantId);

    /**
     * Find billing accounts by billing type
     */
    List<BillingAccount> findByBillingType(BillingType billingType);

    /**
     * Find active billing accounts
     */
    List<BillingAccount> findByIsActiveTrue();

    /**
     * Find suspended billing accounts
     */
    List<BillingAccount> findBySuspendedAtIsNotNull();

    /**
     * Find billing accounts with balance over credit limit
     */
    @Query("SELECT ba FROM BillingAccount ba WHERE ba.currentBalance > ba.creditLimit AND ba.creditLimit IS NOT NULL")
    List<BillingAccount> findAccountsOverCreditLimit();

    /**
     * Find billing accounts by email
     */
    List<BillingAccount> findByEmail(String email);

    /**
     * Search billing accounts by account name or company name
     */
    @Query("SELECT ba FROM BillingAccount ba WHERE " +
           "LOWER(ba.accountName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ba.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "ba.accountNumber LIKE CONCAT('%', :keyword, '%')")
    Page<BillingAccount> searchAccounts(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Get total outstanding balance for all active accounts
     */
    @Query("SELECT COALESCE(SUM(ba.currentBalance), 0) FROM BillingAccount ba WHERE ba.isActive = true")
    BigDecimal getTotalOutstandingBalance();

    /**
     * Count billing accounts by type
     */
    @Query("SELECT COUNT(ba) FROM BillingAccount ba WHERE ba.billingType = :type")
    long countByBillingType(@Param("type") BillingType type);

    /**
     * Find accounts due for suspension (over credit limit)
     */
    @Query("SELECT ba FROM BillingAccount ba WHERE " +
           "ba.isActive = true AND " +
           "ba.creditLimit IS NOT NULL AND " +
           "ba.currentBalance > ba.creditLimit AND " +
           "ba.suspendedAt IS NULL")
    List<BillingAccount> findAccountsNeedingSuspension();
}
