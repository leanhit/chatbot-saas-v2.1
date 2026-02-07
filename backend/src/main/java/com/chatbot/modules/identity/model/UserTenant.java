package com.chatbot.modules.identity.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User-tenant mapping entity - contains ONLY user_id + tenant_id mapping
 * TODO: Add invitation workflow logic
 * TODO: Add membership status transitions
 * v0.2: Updated tenant_id to UUID for consistency with Tenant entity
 */
@Entity
@Table(name = "user_tenants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tenant_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }
    
    // NO role field
    // NO permission field
    // NO business fields
}
