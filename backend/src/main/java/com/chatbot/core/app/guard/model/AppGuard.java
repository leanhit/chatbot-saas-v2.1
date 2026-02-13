package com.chatbot.core.app.guard.model;

import com.chatbot.shared.infrastructure.BaseEntity;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "app_guards")
public class AppGuard extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "app_id", nullable = false)
    private Long appId;
    
    @Column(name = "guard_name", nullable = false)
    private String guardName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "guard_type", nullable = false)
    private GuardType guardType;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "priority")
    private Integer priority = 0;
    
    @OneToMany(mappedBy = "appGuard", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GuardRule> rules = new java.util.ArrayList<>();
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Constructors
    public AppGuard() {}
    
    public AppGuard(Long appId, String guardName, GuardType guardType) {
        this.appId = appId;
        this.guardName = guardName;
        this.guardType = guardType;
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
    
    public String getGuardName() {
        return guardName;
    }
    
    public void setGuardName(String guardName) {
        this.guardName = guardName;
    }
    
    public GuardType getGuardType() {
        return guardType;
    }
    
    public void setGuardType(GuardType guardType) {
        this.guardType = guardType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public List<GuardRule> getRules() {
        return rules;
    }
    
    public void setRules(List<GuardRule> rules) {
        this.rules = rules;
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
