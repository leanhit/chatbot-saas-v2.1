package com.chatbot.core.app.subscription.service;

import com.chatbot.core.app.subscription.dto.SubscriptionResponse;
import com.chatbot.core.app.subscription.dto.SubscribeAppRequest;
import com.chatbot.core.app.subscription.model.AppSubscription;
import com.chatbot.core.app.subscription.model.SubscriptionPlan;
import com.chatbot.core.app.subscription.model.SubscriptionStatus;
import com.chatbot.core.app.subscription.repository.AppSubscriptionRepository;
import com.chatbot.core.app.registry.service.AppRegistryService;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import com.chatbot.shared.exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppSubscriptionService {
    
    @Autowired
    private AppSubscriptionRepository subscriptionRepository;
    
    @Autowired
    private AppRegistryService appRegistryService;
    
    @Autowired
    private SubscriptionValidationService validationService;
    
    public SubscriptionResponse subscribeToApp(SubscribeAppRequest request, Long userId) {
        // Validate app exists and is active
        appRegistryService.getAppById(request.getAppId());
        
        // Check if subscription already exists
        if (subscriptionRepository.existsByAppIdAndTenantId(request.getAppId(), request.getTenantId())) {
            throw new ValidationException("Subscription already exists for this app and tenant");
        }
        
        // Validate subscription request
        validationService.validateSubscriptionRequest(request);
        
        AppSubscription subscription = new AppSubscription();
        subscription.setAppId(request.getAppId());
        subscription.setTenantId(request.getTenantId());
        subscription.setUserId(userId);
        subscription.setSubscriptionPlan(request.getSubscriptionPlan());
        subscription.setAutoRenew(request.getAutoRenew());
        subscription.setCreatedBy(userId);
        
        // Set trial period for free plans
        if (request.getSubscriptionPlan() == SubscriptionPlan.FREE) {
            subscription.setTrialEnd(LocalDateTime.now().plusDays(30));
            subscription.setSubscriptionStatus(SubscriptionStatus.TRIAL);
        } else {
            subscription.setSubscriptionStatus(SubscriptionStatus.PENDING);
        }
        
        // Process subscription based on plan
        validationService.processSubscription(subscription, request.getSubscriptionPlan());
        
        AppSubscription savedSubscription = subscriptionRepository.save(subscription);
        return convertToResponse(savedSubscription);
    }
    
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(Long id) {
        AppSubscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        return convertToResponse(subscription);
    }
    
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionByAppAndTenant(Long appId, Long tenantId) {
        AppSubscription subscription = subscriptionRepository.findByAppIdAndTenantId(appId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found for app and tenant"));
        return convertToResponse(subscription);
    }
    
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByTenant(Long tenantId) {
        List<AppSubscription> subscriptions = subscriptionRepository.findByTenantId(tenantId);
        return subscriptions.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByUser(Long userId) {
        List<AppSubscription> subscriptions = subscriptionRepository.findByUserId(userId);
        return subscriptions.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByApp(Long appId) {
        List<AppSubscription> subscriptions = subscriptionRepository.findByAppId(appId);
        return subscriptions.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    public SubscriptionResponse updateSubscription(Long id, SubscribeAppRequest request, Long updatedBy) {
        AppSubscription existingSubscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        
        // Validate update request
        validationService.validateSubscriptionUpdate(existingSubscription, request);
        
        existingSubscription.setSubscriptionPlan(request.getSubscriptionPlan());
        existingSubscription.setAutoRenew(request.getAutoRenew());
        existingSubscription.setUpdatedBy(updatedBy);
        
        // Process plan change
        validationService.processPlanChange(existingSubscription, request.getSubscriptionPlan());
        
        AppSubscription updatedSubscription = subscriptionRepository.save(existingSubscription);
        return convertToResponse(updatedSubscription);
    }
    
    public SubscriptionResponse updateSubscriptionStatus(Long id, SubscriptionStatus status, Long updatedBy) {
        AppSubscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        
        // Validate status transition
        validationService.validateStatusTransition(subscription.getSubscriptionStatus(), status);
        
        subscription.setSubscriptionStatus(status);
        subscription.setUpdatedBy(updatedBy);
        
        // Process status change
        validationService.processStatusChange(subscription, status);
        
        AppSubscription updatedSubscription = subscriptionRepository.save(subscription);
        return convertToResponse(updatedSubscription);
    }
    
    public void cancelSubscription(Long id, Long updatedBy) {
        AppSubscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        
        subscription.setSubscriptionStatus(SubscriptionStatus.CANCELLED);
        subscription.setAutoRenew(false);
        subscription.setUpdatedBy(updatedBy);
        
        subscriptionRepository.save(subscription);
    }
    
    public void renewSubscription(Long id, Long updatedBy) {
        AppSubscription subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + id));
        
        // Validate renewal eligibility
        validationService.validateRenewalEligibility(subscription);
        
        // Process renewal
        validationService.processRenewal(subscription);
        subscription.setUpdatedBy(updatedBy);
        
        subscriptionRepository.save(subscription);
    }
    
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getExpiredSubscriptions() {
        List<AppSubscription> subscriptions = subscriptionRepository.findExpiredSubscriptions(
            LocalDateTime.now(), SubscriptionStatus.ACTIVE);
        return subscriptions.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getExpiredTrials() {
        List<AppSubscription> subscriptions = subscriptionRepository.findExpiredTrials(
            LocalDateTime.now(), SubscriptionStatus.TRIAL);
        return subscriptions.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    private SubscriptionResponse convertToResponse(AppSubscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(subscription.getId());
        response.setTenantId(subscription.getTenantId());
        response.setUserId(subscription.getUserId());
        response.setSubscriptionPlan(subscription.getSubscriptionPlan());
        response.setSubscriptionStatus(subscription.getSubscriptionStatus());
        response.setSubscriptionStart(subscription.getSubscriptionStart());
        response.setSubscriptionEnd(subscription.getSubscriptionEnd());
        response.setAutoRenew(subscription.getAutoRenew());
        response.setTrialEnd(subscription.getTrialEnd());
        response.setCreatedAt(subscription.getCreatedAt());
        response.setUpdatedAt(subscription.getUpdatedAt());
        response.setCreatedBy(subscription.getCreatedBy());
        response.setUpdatedBy(subscription.getUpdatedBy());
        
        // Convert app details
        try {
            response.setApp(appRegistryService.getAppById(subscription.getAppId()));
        } catch (Exception e) {
            // Handle case where app might be deleted
            // Set a minimal app response or null
        }
        
        return response;
    }
}
