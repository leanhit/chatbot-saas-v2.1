package com.chatbot.core.membership.dto;

import com.chatbot.core.membership.model.TenantRole;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class UpdateMemberRoleRequest {
    @NotNull(message = "Vai trò không được để trống")
    private TenantRole role;
}
