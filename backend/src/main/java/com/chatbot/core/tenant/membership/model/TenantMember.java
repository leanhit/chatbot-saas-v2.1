package com.chatbot.core.tenant.membership.model;

import com.chatbot.core.user.model.User;
import com.chatbot.core.tenant.model.Tenant;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "tenant_members",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "user_id", "status"}),
        @UniqueConstraint(columnNames = {"tenant_id", "user_id"})
    }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết tới Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    // Liên kết tới User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Vai trò trong Tenant (OWNER, ADMIN, MEMBER)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantRole role;

    // Trạng thái thành viên (Thay thế hoàn toàn cho biến active cũ)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MembershipStatus status = MembershipStatus.PENDING;

    // --- Audit fields ---
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method để kiểm tra nhanh quyền truy cập
    public boolean isActiveMember() {
        return MembershipStatus.ACTIVE.equals(this.status);
    }
}