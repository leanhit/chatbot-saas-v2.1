package com.chatbot.core.tenant.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain Event published after a new tenant is created and committed to Tenant Hub DB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantCreatedEvent {
    private Long tenantId;
    private String tenantKey;
    private Long ownerUserId;
    private String currentUserEmail;
}
