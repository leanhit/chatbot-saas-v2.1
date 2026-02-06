package com.chatbot.modules.tenant.core.dto;

// LEGACY CLASS - DISABLED FOR TENANT HUB v0.1
// This class contains detailed response DTO that is not part of v0.1 scope
// TODO: Remove this class completely when v0.1 is stable

/*
import com.chatbot.modules.address.dto.AddressDetailResponseDTO;
import com.chatbot.modules.tenant.profile.dto.TenantProfileResponse;
import com.chatbot.modules.tenant.core.model.TenantStatus;
import com.chatbot.modules.tenant.core.model.TenantVisibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantDetailResponse {
    private Long id;
    private String name;
    private TenantStatus status;
    private TenantVisibility visibility;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private TenantProfileResponse profile;
    private AddressDetailResponseDTO address;

    public static TenantDetailResponse from(
            TenantResponse tenant,
            TenantProfileResponse profile,
            List<AddressDetailResponseDTO> addresses) {
        // Legacy DTO logic - not used in v0.1
        return null;
    }
}
*/
