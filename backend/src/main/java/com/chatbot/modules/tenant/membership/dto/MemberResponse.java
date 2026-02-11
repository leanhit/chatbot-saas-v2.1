package com.chatbot.modules.tenant.membership.dto;

import com.chatbot.modules.tenant.membership.model.MembershipStatus;
import com.chatbot.modules.tenant.membership.model.TenantRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MemberResponse {
    private Long id;
    private Long userId;
    private String email;
    private TenantRole role;
    private MembershipStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime requestedAt;
}
