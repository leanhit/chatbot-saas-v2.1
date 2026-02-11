package com.chatbot.core.tenant.core.mapper;

import com.chatbot.core.tenant.core.dto.CreateTenantRequest;
import com.chatbot.core.tenant.core.dto.TenantResponse;
import com.chatbot.core.tenant.core.model.Tenant;
import com.chatbot.core.tenant.core.model.TenantStatus;
import com.chatbot.core.tenant.profile.model.TenantProfile;
import java.time.LocalDateTime;

public class TenantMapper {

    private TenantMapper() {}

    // Request → Entity
    public static Tenant toEntity(CreateTenantRequest request) {
        return Tenant.builder()
                .name(request.getName())
                .status(TenantStatus.ACTIVE)
                .visibility(request.getVisibility())
                .expiresAt(LocalDateTime.now().plusDays(30)) // trial
                .build();
    }

    // Entity → Response
    public static TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .tenantKey(tenant.getTenantKey()) // ✅ Thay vì id
                .name(tenant.getName())
                .status(tenant.getStatus())
                .visibility(tenant.getVisibility())
                .expiresAt(tenant.getExpiresAt())
                .createdAt(tenant.getCreatedAt())
                .build();
    }

    // Entity → Response with Profile
    public static TenantResponse toResponseWithProfile(Tenant tenant, TenantProfile profile) {
        TenantResponse.TenantResponseBuilder builder = TenantResponse.builder()
                .tenantKey(tenant.getTenantKey())
                .name(tenant.getName())
                .status(tenant.getStatus())
                .visibility(tenant.getVisibility())
                .expiresAt(tenant.getExpiresAt())
                .createdAt(tenant.getCreatedAt());

        // Add profile fields if available
        if (profile != null) {
            builder.logoUrl(profile.getLogoUrl())
                    .contactEmail(profile.getContactEmail())
                    .contactPhone(profile.getContactPhone());
        }

        return builder.build();
    }
}
