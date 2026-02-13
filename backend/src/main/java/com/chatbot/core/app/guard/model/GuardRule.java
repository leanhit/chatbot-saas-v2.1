package com.chatbot.core.app.guard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "guard_rules")
public class GuardRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_guard_id", nullable = false)
    private AppGuard appGuard;
    
    @Column(name = "rule_name", nullable = false)
    private String ruleName;
    
    @Column(name = "rule_condition", nullable = false, columnDefinition = "TEXT")
    private String ruleCondition;
    
    @Column(name = "rule_action", nullable = false)
    private String ruleAction;
    
    @Column(name = "rule_parameters", columnDefinition = "TEXT")
    private String ruleParameters;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "priority")
    private Integer priority = 0;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public GuardRule() {}
    
    public GuardRule(AppGuard appGuard, String ruleName, String ruleCondition, String ruleAction) {
        this.appGuard = appGuard;
        this.ruleName = ruleName;
        this.ruleCondition = ruleCondition;
        this.ruleAction = ruleAction;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public AppGuard getAppGuard() {
        return appGuard;
    }
    
    public void setAppGuard(AppGuard appGuard) {
        this.appGuard = appGuard;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public String getRuleCondition() {
        return ruleCondition;
    }
    
    public void setRuleCondition(String ruleCondition) {
        this.ruleCondition = ruleCondition;
    }
    
    public String getRuleAction() {
        return ruleAction;
    }
    
    public void setRuleAction(String ruleAction) {
        this.ruleAction = ruleAction;
    }
    
    public String getRuleParameters() {
        return ruleParameters;
    }
    
    public void setRuleParameters(String ruleParameters) {
        this.ruleParameters = ruleParameters;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
}
