package com.chatbot.core.membership.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain Event published after a TenantMember is removed or blocked.
 * Used to trigger automatic Agent profile deactivation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDeactivatedEvent {
    /** ID của tenant */
    private Long tenantId;
    /** ID của user bị xóa khỏi tenant */
    private Long userId;
}
