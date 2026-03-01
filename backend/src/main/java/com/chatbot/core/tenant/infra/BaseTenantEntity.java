package com.chatbot.core.tenant.infra;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base entity for all tenant-scoped entities
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseTenantEntity {
    
    // Allow both Long and UUID IDs
    @Column(name = "tenant_key")
    private String tenantKey;
    
    // Add explicit tenantId field for database compatibility
    @Column(name = "tenant_id")
    private Long tenantId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    // Template methods for ID - to be implemented by concrete classes
    public abstract Object getId();
    
    // Tenant ID methods for compatibility
    public Long getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
    
    @PrePersist
    protected void onCreate() {
        // Set tenant_key from context if null
        if (tenantKey == null) {
            tenantKey = TenantContext.getCurrentTenant();
        }
        
        // Set tenant_id from context if null and field exists
        if (this.getTenantId() == null) {
            Long currentTenantId = TenantContext.getTenantId();
            if (currentTenantId != null) {
                this.setTenantId(currentTenantId);
            } else {
                // Fallback to default tenant ID if no context
                this.setTenantId(1L);
            }
        }
        
        if (createdBy == null) {
            createdBy = getCurrentUserId();
        }
        updatedBy = getCurrentUserId();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedBy = getCurrentUserId();
    }
    
    private String getCurrentUserId() {
        // TODO: Get current user from security context
        return "system";
    }
    
    /**
     * Soft delete method
     */
    public void softDelete() {
        this.isDeleted = true;
    }
    
    /**
     * Restore soft deleted entity
     */
    public void restore() {
        this.isDeleted = false;
    }
    
    /**
     * Check if entity is soft deleted
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(isDeleted);
    }
}
