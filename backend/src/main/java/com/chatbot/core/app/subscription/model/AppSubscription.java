package com.chatbot.core.app.subscription.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import com.chatbot.shared.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_subscriptions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class AppSubscription extends BaseTenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "app_id", nullable = false)
    private Long appId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    private SubscriptionPlan subscriptionPlan;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false)
    private SubscriptionStatus subscriptionStatus;
    
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    @Column(name = "subscription_start")
    private LocalDateTime subscriptionStart;
    
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    @Column(name = "subscription_end")
    private LocalDateTime subscriptionEnd;
    
    @Builder.Default
    @Column(name = "auto_renew")
    private Boolean autoRenew = false;
    
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    @Column(name = "trial_end")
    private LocalDateTime trialEnd;
    
    @Column(name = "config_data", columnDefinition = "TEXT")
    private String configData;
    
    @Override
    public Object getId() {
        return id;
    }
    
    @Override
    public Long getTenantId() {
        return super.getTenantId();
    }
    
    @Override
    public void setTenantId(Long tenantId) {
        super.setTenantId(tenantId);
    }
    
    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (subscriptionStart == null) {
            subscriptionStart = LocalDateTime.now();
        }
        if (subscriptionStatus == null) {
            subscriptionStatus = SubscriptionStatus.PENDING;
        }
    }
}
