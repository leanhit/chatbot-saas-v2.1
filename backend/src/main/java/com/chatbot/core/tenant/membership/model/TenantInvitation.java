package com.chatbot.core.tenant.membership.model;

import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.identity.model.Auth;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    
    @ManyToOne
    @JoinColumn(name = "invited_by")
    private Auth invitedBy;
}
