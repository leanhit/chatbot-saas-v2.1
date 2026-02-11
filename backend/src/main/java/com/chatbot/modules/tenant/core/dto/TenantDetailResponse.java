package com.chatbot.modules.tenant.core.dto;

import com.chatbot.modules.address.dto.AddressDetailResponseDTO;
import com.chatbot.modules.tenant.profile.dto.TenantProfileResponse;
import com.chatbot.modules.tenant.core.model.TenantStatus;
import com.chatbot.modules.tenant.core.model.TenantVisibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO chứa đầy đủ thông tin tenant bao gồm profile và địa chỉ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantDetailResponse {
    private String tenantKey; // ✅ Thay vì id
    private String name;
    private TenantStatus status;
    private TenantVisibility visibility;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private TenantProfileResponse profile;
    private AddressDetailResponseDTO address;

    /**
     * Tạo đối tượng TenantDetailResponse từ các thành phần.
     */
    public static TenantDetailResponse from(
            TenantResponse tenant,
            TenantProfileResponse profile,
            AddressDetailResponseDTO address) {
        
        TenantDetailResponse response = new TenantDetailResponse();
        response.setTenantKey(tenant.getTenantKey()); // ✅ Thay vì getId()
        response.setName(tenant.getName());
        response.setStatus(tenant.getStatus());
        response.setVisibility(tenant.getVisibility());
        response.setExpiresAt(tenant.getExpiresAt());
        response.setCreatedAt(tenant.getCreatedAt());
        response.setProfile(profile);
        response.setAddress(address);
        return response;
    }

    /**
     * Tạo đối tượng TenantDetailResponse từ các thành phần (để tương thích với code cũ).
     */
    public static TenantDetailResponse from(
            TenantResponse tenant,
            TenantProfileResponse profile,
            List<AddressDetailResponseDTO> addresses) {
        
        TenantDetailResponse response = new TenantDetailResponse();
        response.setTenantKey(tenant.getTenantKey()); // ✅ Thay vì getId()
        response.setName(tenant.getName());
        response.setStatus(tenant.getStatus());
        response.setVisibility(tenant.getVisibility());
        response.setExpiresAt(tenant.getExpiresAt());
        response.setCreatedAt(tenant.getCreatedAt());
        response.setProfile(profile);
        // Lấy địa chỉ đầu tiên nếu có, không thì để null
        response.setAddress(addresses != null && !addresses.isEmpty() ? addresses.get(0) : null);
        return response;
    }
}
