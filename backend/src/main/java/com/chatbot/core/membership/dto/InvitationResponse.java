package com.chatbot.core.membership.dto;

import com.chatbot.core.membership.model.InvitationStatus;
import com.chatbot.core.membership.model.TenantRole;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// InvitationResponse.java
@Getter
@Builder
@Setter
public class InvitationResponse {
    private Long id;
    private String name; // Tenant name for backwards compatibility
    private String tenantName;
    private String tenantKey;
    private String email;
    private TenantRole role;
    private InvitationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String invitedByName;
    private String token; // Add token for accept/reject functionality
}