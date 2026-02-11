package com.chatbot.core.tenant.core.dto;

import com.chatbot.core.tenant.core.model.TenantVisibility;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateTenantRequest {
    @NotBlank
    private String name;

    @NotNull(message = "Visibility cannot be null")
    private TenantVisibility visibility;
}
