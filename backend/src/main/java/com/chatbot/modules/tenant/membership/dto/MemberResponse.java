package com.chatbot.modules.tenant.membership.dto;

import com.chatbot.modules.tenant.membership.model.TenantRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Member response DTO for v0.1
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponse {

    private UUID id;
    private UUID userId;
    private String email;
    private TenantRole role;
    private LocalDateTime joinedAt;
}
