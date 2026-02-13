package com.chatbot.core.app.subscription.service;

import com.chatbot.core.app.subscription.dto.SubscribeAppRequest;
import com.chatbot.core.app.subscription.model.AppSubscription;
import com.chatbot.core.app.subscription.model.SubscriptionPlan;
import com.chatbot.core.app.subscription.model.SubscriptionStatus;
import com.chatbot.shared.exceptions.ValidationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SubscriptionValidationService {
    
    public void validateSubscriptionRequest(SubscribeAppRequest request) {
        if (request.getAppId() == null || request.getAppId() <= 0) {
            throw new ValidationException("Valid app ID is required");
        }
        
        if (request.getTenantId() == null || request.getTenantId() <= 0) {
            throw new ValidationException("Valid tenant ID is required");
        }
        
        if (request.getSubscriptionPlan() == null) {
            throw new ValidationException("Subscription plan is required");
        }
    }
    
    public void validateSubscriptionUpdate(AppSubscription existingSubscription, SubscribeAppRequest request) {
        // Check if subscription can be updated
        if (existingSubscription.getSubscriptionStatus() == SubscriptionStatus.CANCELLED) {
            throw new ValidationException("Cannot update cancelled subscription");
        }
        
        if (existingSubscription.getSubscriptionStatus() == SubscriptionStatus.EXPIRED) {
            throw new ValidationException("Cannot update expired subscription. Please renew instead.");
        }
    }
    
    public void validateStatusTransition(SubscriptionStatus currentStatus, SubscriptionStatus newStatus) {
        // Define allowed transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != SubscriptionStatus.ACTIVE && newStatus != SubscriptionStatus.CANCELLED) {
                    throw new ValidationException("Invalid status transition from PENDING");
                }
                break;
            case TRIAL:
                if (newStatus != SubscriptionStatus.ACTIVE && newStatus != SubscriptionStatus.EXPIRED && 
                    newStatus != SubscriptionStatus.CANCELLED) {
                    throw new ValidationException("Invalid status transition from TRIAL");
                }
                break;
            case ACTIVE:
                if (newStatus != SubscriptionStatus.INACTIVE && newStatus != SubscriptionStatus.SUSPENDED && 
                    newStatus != SubscriptionStatus.CANCELLED && newStatus != SubscriptionStatus.EXPIRED) {
                    throw new ValidationException("Invalid status transition from ACTIVE");
                }
                break;
            case INACTIVE:
                if (newStatus != SubscriptionStatus.ACTIVE && newStatus != SubscriptionStatus.CANCELLED) {
                    throw new ValidationException("Invalid status transition from INACTIVE");
                }
                break;
            case SUSPENDED:
                if (newStatus != SubscriptionStatus.ACTIVE && newStatus != SubscriptionStatus.CANCELLED) {
                    throw new ValidationException("Invalid status transition from SUSPENDED");
                }
                break;
            case CANCELLED:
            case EXPIRED:
                throw new ValidationException("Cannot change status from " + currentStatus.getDisplayName());
        }
    }
    
    public void validateRenewalEligibility(AppSubscription subscription) {
        if (subscription.getSubscriptionStatus() != SubscriptionStatus.EXPIRED && 
            subscription.getSubscriptionStatus() != SubscriptionStatus.ACTIVE) {
            throw new ValidationException("Subscription is not eligible for renewal");
        }
        
        if (subscription.getSubscriptionEnd() != null && 
            subscription.getSubscriptionEnd().isAfter(LocalDateTime.now().plusDays(30))) {
            throw new ValidationException("Subscription is not due for renewal yet");
        }
    }
    
    public void processSubscription(AppSubscription subscription, SubscriptionPlan plan) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (plan) {
            case FREE:
                // Free plan with 30-day trial
                subscription.setTrialEnd(now.plusDays(30));
                subscription.setSubscriptionStart(now);
                subscription.setSubscriptionEnd(now.plusDays(30));
                break;
                
            case BASIC:
                // Basic plan - 1 month
                subscription.setSubscriptionStart(now);
                subscription.setSubscriptionEnd(now.plusMonths(1));
                break;
                
            case PROFESSIONAL:
                // Professional plan - 1 month
                subscription.setSubscriptionStart(now);
                subscription.setSubscriptionEnd(now.plusMonths(1));
                break;
                
            case ENTERPRISE:
                // Enterprise plan - 1 year
                subscription.setSubscriptionStart(now);
                subscription.setSubscriptionEnd(now.plusYears(1));
                break;
                
            case CUSTOM:
                // Custom plan - set to pending for manual configuration
                subscription.setSubscriptionStart(now);
                // End date will be set manually
                break;
        }
    }
    
    public void processPlanChange(AppSubscription subscription, SubscriptionPlan newPlan) {
        LocalDateTime now = LocalDateTime.now();
        
        // Calculate prorated period if changing from paid plan
        if (subscription.getSubscriptionEnd() != null && 
            subscription.getSubscriptionStatus() == SubscriptionStatus.ACTIVE) {
            
            switch (newPlan) {
                case FREE:
                    subscription.setSubscriptionEnd(now.plusDays(30));
                    break;
                case BASIC:
                case PROFESSIONAL:
                    subscription.setSubscriptionEnd(now.plusMonths(1));
                    break;
                case ENTERPRISE:
                    subscription.setSubscriptionEnd(now.plusYears(1));
                    break;
                case CUSTOM:
                    // Keep existing end date for custom plans
                    break;
            }
        }
    }
    
    public void processStatusChange(AppSubscription subscription, SubscriptionStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (newStatus) {
            case ACTIVE:
                if (subscription.getSubscriptionStart() == null) {
                    subscription.setSubscriptionStart(now);
                }
                if (subscription.getSubscriptionEnd() == null) {
                    subscription.setSubscriptionEnd(now.plusMonths(1));
                }
                break;
                
            case CANCELLED:
                // Set end date to now if not already set
                if (subscription.getSubscriptionEnd() == null || 
                    subscription.getSubscriptionEnd().isAfter(now)) {
                    subscription.setSubscriptionEnd(now);
                }
                subscription.setAutoRenew(false);
                break;
                
            case EXPIRED:
                // Ensure end date is set to now or in the past
                if (subscription.getSubscriptionEnd() == null || 
                    subscription.getSubscriptionEnd().isAfter(now)) {
                    subscription.setSubscriptionEnd(now);
                }
                break;
        }
    }
    
    public void processRenewal(AppSubscription subscription) {
        LocalDateTime now = LocalDateTime.now();
        SubscriptionPlan plan = subscription.getSubscriptionPlan();
        
        // Extend subscription based on plan
        switch (plan) {
            case FREE:
                subscription.setSubscriptionEnd(now.plusDays(30));
                break;
            case BASIC:
            case PROFESSIONAL:
                subscription.setSubscriptionEnd(now.plusMonths(1));
                break;
            case ENTERPRISE:
                subscription.setSubscriptionEnd(now.plusYears(1));
                break;
            case CUSTOM:
                // For custom plans, extend by 1 month by default
                subscription.setSubscriptionEnd(now.plusMonths(1));
                break;
        }
        
        subscription.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
    }
}
