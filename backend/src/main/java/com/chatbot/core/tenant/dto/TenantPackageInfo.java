package com.chatbot.core.tenant.dto;

import com.chatbot.core.payment.plan.model.Package;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantPackageInfo {
    
    @JsonIgnore
    private Long tenantId;
    private Package packageInfo;
    private LocalDateTime packageActivatedAt;
    private LocalDateTime packageExpiresAt;
    private String expiresAtDisplay;
    private boolean isExpired;
    private boolean isFree;
    
    // Helper method to create from tenant and package
    public static TenantPackageInfo from(Long tenantId, Package packageInfo, LocalDateTime activatedAt, LocalDateTime expiresAt) {
        if (packageInfo == null) {
            return TenantPackageInfo.builder()
                    .tenantId(tenantId)
                    .packageInfo(null)
                    .packageActivatedAt(activatedAt)
                    .packageExpiresAt(null)
                    .expiresAtDisplay("N/A")
                    .isExpired(false)
                    .isFree(true)
                    .build();
        }
        
        boolean isFree = "free".equals(packageInfo.getPackageId()) || packageInfo.getPrice().doubleValue() == 0;
        boolean isExpired = !isFree && expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
        
        return TenantPackageInfo.builder()
                .tenantId(tenantId)
                .packageInfo(packageInfo)
                .packageActivatedAt(activatedAt)
                .packageExpiresAt(expiresAt)
                .expiresAtDisplay(isFree ? "N/A" : (expiresAt != null ? expiresAt.toString() : "N/A"))
                .isExpired(isExpired)
                .isFree(isFree)
                .build();
    }
}
