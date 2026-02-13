package com.chatbot.core.app.subscription.dto;

import com.chatbot.core.app.subscription.model.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class SubscribeAppRequest {
    
    @NotNull(message = "App ID is required")
    private Long appId;
    
    @NotNull(message = "Tenant ID is required")
    private Long tenantId;
    
    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan subscriptionPlan;
    
    private Boolean autoRenew = false;
    
    private Map<String, Object> configData;
    
    // Constructors
    public SubscribeAppRequest() {}
    
    public SubscribeAppRequest(Long appId, Long tenantId, SubscriptionPlan subscriptionPlan) {
        this.appId = appId;
        this.tenantId = tenantId;
        this.subscriptionPlan = subscriptionPlan;
    }
    
    // Getters and Setters
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
    
    public SubscriptionPlan getSubscriptionPlan() {
        return subscriptionPlan;
    }
    
    public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }
    
    public Boolean getAutoRenew() {
        return autoRenew;
    }
    
    public void setAutoRenew(Boolean autoRenew) {
        this.autoRenew = autoRenew;
    }
    
    public Map<String, Object> getConfigData() {
        return configData;
    }
    
    public void setConfigData(Map<String, Object> configData) {
        this.configData = configData;
    }
}
