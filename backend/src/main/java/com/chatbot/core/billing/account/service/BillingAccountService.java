package com.chatbot.core.billing.account.service;

import com.chatbot.core.billing.account.dto.BillingAccountRequest;
import com.chatbot.core.billing.account.dto.BillingAccountResponse;
import com.chatbot.core.billing.account.model.BillingAccount;
import com.chatbot.core.billing.account.repository.BillingAccountRepository;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingAccountService {

    private final BillingAccountRepository billingAccountRepository;

    /**
     * Create new billing account for tenant
     */
    @Transactional
    public BillingAccountResponse createBillingAccount(Long tenantId, BillingAccountRequest request) {
        log.info("Creating billing account for tenant: {}", tenantId);

        // Check if billing account already exists for tenant
        if (billingAccountRepository.existsByTenantId(tenantId)) {
            throw new IllegalStateException("Billing account already exists for tenant");
        }

        BillingAccount billingAccount = BillingAccount.builder()
                .accountName(request.getAccountName())
                .companyName(request.getCompanyName())
                .taxCode(request.getTaxCode())
                .email(request.getEmail())
                .phone(request.getPhone())
                .billingAddress(request.getBillingAddress())
                .currency(request.getCurrency())
                .creditLimit(request.getCreditLimit())
                .autoPayment(request.getAutoPayment())
                .paymentMethodToken(request.getPaymentMethodToken())
                .billingType(request.getBillingType())
                .build();

        // Set tenantId separately since it's inherited from BaseTenantEntity
        billingAccount.setTenantId(tenantId);

        BillingAccount saved = billingAccountRepository.save(billingAccount);
        log.info("Created billing account: {} for tenant: {}", saved.getAccountNumber(), tenantId);

        return mapToResponse(saved);
    }

    /**
     * Get billing account by tenant ID
     */
    public BillingAccountResponse getBillingAccountByTenant(Long tenantId) {
        BillingAccount account = billingAccountRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found for tenant: " + tenantId));
        return mapToResponse(account);
    }

    /**
     * Get billing account by ID
     */
    public BillingAccountResponse getBillingAccountById(Long id) {
        BillingAccount account = billingAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found: " + id));
        return mapToResponse(account);
    }

    /**
     * Update billing account
     */
    @Transactional
    public BillingAccountResponse updateBillingAccount(Long id, BillingAccountRequest request) {
        BillingAccount account = billingAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found: " + id));

        account.setAccountName(request.getAccountName());
        account.setCompanyName(request.getCompanyName());
        account.setTaxCode(request.getTaxCode());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setBillingAddress(request.getBillingAddress());
        account.setCurrency(request.getCurrency());
        account.setCreditLimit(request.getCreditLimit());
        account.setAutoPayment(request.getAutoPayment());
        account.setPaymentMethodToken(request.getPaymentMethodToken());
        account.setBillingType(request.getBillingType());

        BillingAccount updated = billingAccountRepository.save(account);
        log.info("Updated billing account: {}", updated.getAccountNumber());

        return mapToResponse(updated);
    }

    /**
     * Update account balance
     */
    @Transactional
    public void updateBalance(Long accountId, BigDecimal amount) {
        BillingAccount account = billingAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found: " + accountId));

        BigDecimal newBalance = account.getCurrentBalance().add(amount);
        account.setCurrentBalance(newBalance);

        billingAccountRepository.save(account);
        log.info("Updated balance for account {}: {} -> {}", 
                account.getAccountNumber(), account.getCurrentBalance(), newBalance);

        // Check if account is over credit limit
        if (account.isOverCreditLimit() && !account.isSuspended()) {
            log.warn("Account {} is over credit limit", account.getAccountNumber());
            // TODO: Trigger suspension process
        }
    }

    /**
     * Suspend billing account
     */
    @Transactional
    public void suspendBillingAccount(Long id, String reason) {
        BillingAccount account = billingAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found: " + id));

        account.setIsActive(false);
        account.setSuspendedAt(java.time.LocalDateTime.now());
        account.setSuspensionReason(reason);

        billingAccountRepository.save(account);
        log.info("Suspended billing account: {} for reason: {}", account.getAccountNumber(), reason);
    }

    /**
     * Reactivate billing account
     */
    @Transactional
    public void reactivateBillingAccount(Long id) {
        BillingAccount account = billingAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Billing account not found: " + id));

        account.setIsActive(true);
        account.setSuspendedAt(null);
        account.setSuspensionReason(null);

        billingAccountRepository.save(account);
        log.info("Reactivated billing account: {}", account.getAccountNumber());
    }

    /**
     * Search billing accounts
     */
    public Page<BillingAccountResponse> searchAccounts(String keyword, Pageable pageable) {
        return billingAccountRepository.searchAccounts(keyword, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get all active billing accounts
     */
    public List<BillingAccountResponse> getActiveAccounts() {
        return billingAccountRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get accounts needing suspension
     */
    public List<BillingAccountResponse> getAccountsNeedingSuspension() {
        return billingAccountRepository.findAccountsNeedingSuspension().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get total outstanding balance
     */
    public BigDecimal getTotalOutstandingBalance() {
        return billingAccountRepository.getTotalOutstandingBalance();
    }

    private BillingAccountResponse mapToResponse(BillingAccount account) {
        return BillingAccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .billingType(account.getBillingType())
                .accountName(account.getAccountName())
                .companyName(account.getCompanyName())
                .taxCode(account.getTaxCode())
                .email(account.getEmail())
                .phone(account.getPhone())
                .billingAddress(account.getBillingAddress())
                .currency(account.getCurrency())
                .creditLimit(account.getCreditLimit())
                .currentBalance(account.getCurrentBalance())
                .availableCredit(account.getAvailableCredit())
                .autoPayment(account.getAutoPayment())
                .isActive(account.getIsActive())
                .isSuspended(account.isSuspended())
                .suspendedAt(account.getSuspendedAt())
                .suspensionReason(account.getSuspensionReason())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .tenantId(account.getTenantId())
                .build();
    }
}
