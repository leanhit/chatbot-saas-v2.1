package com.chatbot.modules.tenant.membership.model;

import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.tenant.core.model.Tenant;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TenantMember entity - User-tenant relationship
 * 
 * v0.1: Simplified membership model
 * - UUID primary key
 * - Primitive identity fields (no User entity dependency)
 * - Basic role management only
 */
@Entity
@Table(
    name = "tenant_members",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "user_id"})
    }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (joinedAt == null) {
            joinedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic helpers
    public boolean isOwner() {
        return TenantRole.OWNER.equals(this.role);
    }

    public boolean canManageMembers() {
        return TenantRole.OWNER.equals(this.role) || TenantRole.ADMIN.equals(this.role);
    }
}