package com.chatbot.modules.tenant.membership.dto;

import com.chatbot.modules.tenant.membership.model.TenantRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// InviteMemberRequest.java
@Getter
@Setter
public class InviteMemberRequest {
    @NotBlank
    @Email
    private String email;
    
    @NotNull
    private TenantRole role;
    
    @Min(1)
    @Max(30)
    private int expiryDays = 7; // Mặc định hết hạn sau 7 ngày
}
