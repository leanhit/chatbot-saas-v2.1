package com.chatbot.core.membership.dto;

import com.chatbot.core.membership.model.MembershipStatus;
import com.chatbot.core.membership.model.TenantRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MemberResponse {
    private Long id;
    private Long userId;
    private String email;
    private String name;
    private String avatar;
    private TenantRole role;
    private MembershipStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime requestedAt;
}
