package com.chatbot.core.tenant.membership.dto;

import com.chatbot.core.tenant.membership.model.InvitationStatus;
import com.chatbot.core.tenant.membership.model.TenantRole;

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
    private String name;
    private String email;
    private TenantRole role;
    private InvitationStatus status;
    private LocalDateTime expiresAt;
    private String invitedByName;
}