package com.chatbot.spokes.odoo.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.chatbot.shared.utils.DateUtils;
import java.time.LocalDateTime;

@Entity
@Table(name = "fb_captured_phone")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FbCapturedPhone extends BaseTenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "owner_id")
    private String ownerId;

    // QUAN TRỌNG: unique = true đảm bảo không có SĐT trùng lặp
    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime createdAt;
    
    @Column(name = "tenant_id")
    private Long tenantId;
    
    @Override
    public Long getId() {
        return id;
    }
    
    @Override
    public Long getTenantId() {
        return tenantId;
    }
    
    @Override
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}