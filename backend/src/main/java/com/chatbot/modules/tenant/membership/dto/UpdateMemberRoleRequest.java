package com.chatbot.modules.tenant.membership.dto;

import com.chatbot.modules.tenant.membership.model.TenantRole;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class UpdateMemberRoleRequest {
    @NotNull(message = "Vai trò không được để trống")
    private TenantRole role;
}
