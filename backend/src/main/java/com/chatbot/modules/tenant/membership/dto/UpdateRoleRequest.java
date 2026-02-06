package com.chatbot.modules.tenant.membership.dto;

import com.chatbot.modules.tenant.membership.model.TenantRole;
import lombok.Getter;

@Getter
public class UpdateRoleRequest {
    private TenantRole role;
}
