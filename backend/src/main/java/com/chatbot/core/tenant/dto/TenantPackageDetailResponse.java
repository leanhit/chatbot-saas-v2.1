package com.chatbot.core.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantPackageDetailResponse {
    private Long tenantId;
    private String tenantKey;
    private String currentPackageId;
    private LocalDateTime packageActivatedAt;
    private LocalDateTime expiresAt;
    private String packageName;
    private BigDecimal packagePrice;
    private String packageCurrency;
    private String packageDuration;
    private Integer chatbotLimit;
    private Integer messageLimit;
}
