package com.chatbot.core.tenant.mapper;

import com.chatbot.core.tenant.dto.CreateTenantRequest;
import com.chatbot.core.tenant.dto.TenantResponse;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.profile.model.TenantProfile;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;

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

    // Entity → Response (convert LocalDateTime → Instant)
    public static TenantResponse toResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .tenantKey(tenant.getTenantKey())
                .name(tenant.getName())
                .status(tenant.getStatus())
                .visibility(tenant.getVisibility())
                .expiresAt(toInstant(tenant.getExpiresAt()))
                .createdAt(toInstant(tenant.getCreatedAt()))
                .currentPackageId(tenant.getCurrentPackageId())
                .currentPackageName(getPackageName(tenant.getCurrentPackageId()))
                .packageActivatedAt(toInstant(tenant.getPackageActivatedAt()))
                .build();
    }

    // Entity → Response with Profile (convert LocalDateTime → Instant)
    public static TenantResponse toResponseWithProfile(Tenant tenant, TenantProfile profile) {
        TenantResponse.TenantResponseBuilder builder = TenantResponse.builder()
                .id(tenant.getId())
                .tenantKey(tenant.getTenantKey())
                .name(tenant.getName())
                .status(tenant.getStatus())
                .visibility(tenant.getVisibility())
                .expiresAt(toInstant(tenant.getExpiresAt()))
                .createdAt(toInstant(tenant.getCreatedAt()));

        // Add profile fields if available
        if (profile != null) {
            builder.logoUrl(profile.getLogoUrl())
                    .contactEmail(profile.getContactEmail())
                    .contactPhone(profile.getContactPhone());
        }

        // Add package fields
        builder.currentPackageId(tenant.getCurrentPackageId())
                .currentPackageName(getPackageName(tenant.getCurrentPackageId()))
                .packageActivatedAt(toInstant(tenant.getPackageActivatedAt()));

        return builder.build();
    }

    // Helper method: LocalDateTime → Instant
    private static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime != null ? 
            localDateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
    }

    // Helper method: Get package name from package ID
    private static String getPackageName(String packageId) {
        if (packageId == null) return null;
        
        switch (packageId.toLowerCase()) {
            case "free":
                return "Free";
            case "pro":
                return "Pro";
            case "business":
                return "Business";
            case "enterprise":
                return "Enterprise";
            default:
                return packageId;
        }
    }
}
