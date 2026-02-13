package com.chatbot.core.app.subscription.model;

import com.chatbot.shared.infrastructure.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_subscriptions")
public class AppSubscription extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "app_id", nullable = false)
    private Long appId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    private SubscriptionPlan subscriptionPlan;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false)
    private SubscriptionStatus subscriptionStatus;
    
    @Column(name = "subscription_start")
    private LocalDateTime subscriptionStart;
    
    @Column(name = "subscription_end")
    private LocalDateTime subscriptionEnd;
    
    @Column(name = "auto_renew")
    private Boolean autoRenew = false;
    
    @Column(name = "trial_end")
    private LocalDateTime trialEnd;
    
    @Column(name = "config_data", columnDefinition = "TEXT")
    private String configData;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Constructors
    public AppSubscription() {}
    
    public AppSubscription(Long appId, Long tenantId, Long userId, SubscriptionPlan subscriptionPlan) {
        this.appId = appId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.subscriptionPlan = subscriptionPlan;
        this.subscriptionStatus = SubscriptionStatus.PENDING;
        this.subscriptionStart = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAppId() {
        return appId;
    }
    
    public void setAppId(Long appId) {
        this.appId = appId;
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
    
    public String getConfigData() {
        return configData;
    }
    
    public void setConfigData(String configData) {
        this.configData = configData;
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
