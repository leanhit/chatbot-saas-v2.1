package com.chatbot.modules.tenant.membership.model;

import com.chatbot.modules.tenant.core.model.Tenant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// TenantInvitation.java
@Entity
@Table(name = "tenant_invitations")
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TenantRole role;
    
    @Column(nullable = false)
    private String token;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InvitationStatus status;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime expiresAt;
    
    @Column(name = "invited_by")
    private UUID invitedBy;
}
