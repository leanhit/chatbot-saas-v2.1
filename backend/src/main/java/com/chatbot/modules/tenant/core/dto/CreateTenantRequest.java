package com.chatbot.modules.tenant.core.dto;

import com.chatbot.modules.tenant.core.model.TenantVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Create tenant request DTO for v0.1
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTenantRequest {

    @NotBlank(message = "Tenant name is required")
    @Size(min = 2, max = 255, message = "Tenant name must be between 2 and 255 characters")
    private String name;
    
    private TenantVisibility visibility;
}
