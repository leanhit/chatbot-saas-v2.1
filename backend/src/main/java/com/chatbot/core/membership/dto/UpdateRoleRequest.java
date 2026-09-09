package com.chatbot.core.membership.dto;

import com.chatbot.core.membership.model.TenantRole;
import lombok.Getter;

@Getter
public class UpdateRoleRequest {
    private TenantRole role;
}
