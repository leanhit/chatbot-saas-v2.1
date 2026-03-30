package com.chatbot.core.billing.security;

import com.chatbot.core.billing.account.model.BillingAccount;
import com.chatbot.core.billing.account.repository.BillingAccountRepository;
import com.chatbot.core.billing.subscription.model.BillingSubscription;
import com.chatbot.core.billing.subscription.repository.BillingSubscriptionRepository;
import com.chatbot.core.tenant.security.TenantSecurityEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component("billingSecurity")
@RequiredArgsConstructor
public class BillingSecurityEvaluator {

    private final BillingAccountRepository billingAccountRepository;
    private final BillingSubscriptionRepository billingSubscriptionRepository;
    private final TenantSecurityEvaluator tenantSecurity;

    /**
     * Check if user can access billing account
     */
    public boolean canAccessAccount(Long accountId) {
        try {
            BillingAccount account = billingAccountRepository.findById(accountId).orElse(null);
            if (account == null) {
                return false;
            }

            // Admin can access all accounts
            if (tenantSecurity.isAdmin()) {
                return true;
            }

            // Tenant members can access their own accounts
            return tenantSecurity.isTenantMember(account.getTenantId());
        } catch (Exception e) {
            log.error("Error checking billing account access", e);
            return false;
        }
    }

    /**
     * Check if user can manage billing account
     */
    public boolean canManageAccount(Long accountId) {
        try {
            BillingAccount account = billingAccountRepository.findById(accountId).orElse(null);
            if (account == null) {
                return false;
            }

            // Admin can manage all accounts
            if (tenantSecurity.isAdmin()) {
                return true;
            }

            // Tenant owners can manage their accounts
            return tenantSecurity.isTenantOwner(account.getTenantId());
        } catch (Exception e) {
            log.error("Error checking billing account management", e);
            return false;
        }
    }

    /**
     * Check if user can access billing subscription
     */
    public boolean canAccessSubscription(Long subscriptionId) {
        try {
            BillingSubscription subscription = billingSubscriptionRepository.findById(subscriptionId).orElse(null);
            if (subscription == null) {
                return false;
            }

            // Admin can access all subscriptions
            if (tenantSecurity.isAdmin()) {
                return true;
            }

            // Tenant members can access their own subscriptions
            return tenantSecurity.isTenantMember(subscription.getTenantId());
        } catch (Exception e) {
            log.error("Error checking billing subscription access", e);
            return false;
        }
    }

    /**
     * Check if user can manage billing subscription
     */
    public boolean canManageSubscription(Long subscriptionId) {
        try {
            BillingSubscription subscription = billingSubscriptionRepository.findById(subscriptionId).orElse(null);
            if (subscription == null) {
                return false;
            }

            // Admin can manage all subscriptions
            if (tenantSecurity.isAdmin()) {
                return true;
            }

            // Tenant owners can manage their subscriptions
            return tenantSecurity.isTenantOwner(subscription.getTenantId());
        } catch (Exception e) {
            log.error("Error checking billing subscription management", e);
            return false;
        }
    }
}
