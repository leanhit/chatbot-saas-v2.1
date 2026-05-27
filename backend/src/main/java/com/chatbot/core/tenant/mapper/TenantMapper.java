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
    public static Tenant toEntity(CreateTenantRequest request, int trialDays) {
        return Tenant.builder()
                .name(request.getName())
                .status(TenantStatus.ACTIVE)
                .visibility(request.getVisibility())
                .expiresAt(LocalDateTime.now().plusDays(trialDays))
                .build();
    }

    // Entity → Response
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
                // currentPackageName: lấy từ DB qua PackageService nếu cần.
                // Để tránh hardcode, trả về packageId — frontend tự resolve tên hiển thị.
                .currentPackageName(tenant.getCurrentPackageId())
                .packageActivatedAt(toInstant(tenant.getPackageActivatedAt()))
                .build();
    }

    // Entity → Response kèm Profile
    public static TenantResponse toResponseWithProfile(Tenant tenant, TenantProfile profile) {
        TenantResponse.TenantResponseBuilder builder = TenantResponse.builder()
                .id(tenant.getId())
                .tenantKey(tenant.getTenantKey())
                .name(tenant.getName())
                .status(tenant.getStatus())
                .visibility(tenant.getVisibility())
                .expiresAt(toInstant(tenant.getExpiresAt()))
                .createdAt(toInstant(tenant.getCreatedAt()))
                .currentPackageId(tenant.getCurrentPackageId())
                .currentPackageName(tenant.getCurrentPackageId())
                .packageActivatedAt(toInstant(tenant.getPackageActivatedAt()));

        if (profile != null) {
            builder.logoUrl(profile.getLogoUrl())
                   .contactEmail(profile.getContactEmail())
                   .contactPhone(profile.getContactPhone())
                   .website(profile.getWebsite());
        }

        return builder.build();
    }

    // Helper: LocalDateTime → Instant (timezone hệ thống)
    private static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime != null
                ? localDateTime.atZone(ZoneId.systemDefault()).toInstant()
                : null;
    }
}
