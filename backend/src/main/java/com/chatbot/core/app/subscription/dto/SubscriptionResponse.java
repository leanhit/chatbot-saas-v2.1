package com.chatbot.core.app.subscription.dto;

import com.chatbot.core.app.subscription.model.SubscriptionPlan;
import com.chatbot.core.app.subscription.model.SubscriptionStatus;
import com.chatbot.core.app.registry.dto.AppResponse;
import java.time.LocalDateTime;
import java.util.Map;

public class SubscriptionResponse {
    
    private Long id;
    private AppResponse app;
    private Long tenantId;
    private Long userId;
    private SubscriptionPlan subscriptionPlan;
    private SubscriptionStatus subscriptionStatus;
    private LocalDateTime subscriptionStart;
    private LocalDateTime subscriptionEnd;
    private Boolean autoRenew;
    private LocalDateTime trialEnd;
    private Map<String, Object> configData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    
    // Constructors
    public SubscriptionResponse() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public AppResponse getApp() {
        return app;
    }
    
    public void setApp(AppResponse app) {
        this.app = app;
    }
    
    public Long getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }
    
    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }
    
    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }
    
    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }
    
    public LocalDateTime getSubscriptionStart() {
        return subscriptionStart;
    }
    
    public void setSubscriptionStart(LocalDateTime subscriptionStart) {
        this.subscriptionStart = subscriptionStart;
    }
    
    public LocalDateTime getSubscriptionEnd() {
        return subscriptionEnd;
    }
    
    public void setSubscriptionEnd(LocalDateTime subscriptionEnd) {
        this.subscriptionEnd = subscriptionEnd;
    }
    
    public Boolean getAutoRenew() {
        return autoRenew;
    }
    
    public void setAutoRenew(Boolean autoRenew) {
        this.autoRenew = autoRenew;
    }
    
    public LocalDateTime getTrialEnd() {
        return trialEnd;
    }
    
    public void setTrialEnd(LocalDateTime trialEnd) {
        this.trialEnd = trialEnd;
    }
    
    public Map<String, Object> getConfigData() {
        return configData;
    }
    
    public void setConfigData(Map<String, Object> configData) {
        this.configData = configData;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public Long getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
}
