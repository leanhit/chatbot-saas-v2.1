package com.chatbot.core.membership.event;

import com.chatbot.core.membership.model.TenantRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain Event published after a TenantMember's status transitions to ACTIVE.
 * Used to trigger automatic Agent profile provisioning.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberActivatedEvent {
    /** ID của tenant mà member vừa được kích hoạt */
    private Long tenantId;
    /** ID của user account (shared DB) */
    private Long userId;
    /** Email của user */
    private String userEmail;
    /** Tên đầy đủ của user */
    private String userName;
    /** URL avatar của user */
    private String userAvatar;
    /** Vai trò trong tenant */
    private TenantRole tenantRole;
}
