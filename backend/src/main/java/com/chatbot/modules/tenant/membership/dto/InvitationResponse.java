package com.chatbot.modules.tenant.membership.dto;

import com.chatbot.modules.tenant.membership.model.InvitationStatus;
import com.chatbot.modules.tenant.membership.model.TenantRole;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

// InvitationResponse.java
@Getter
@Builder
@Setter
public class InvitationResponse {
    private Long id;
    private UUID tenantId;
    private String name;
    private String email;
    private TenantRole role;
    private InvitationStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime invitedAt;
    private Long invitedBy;
    private String invitedByName;
}