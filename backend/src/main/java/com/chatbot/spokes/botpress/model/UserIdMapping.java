package com.chatbot.spokes.botpress.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "user_id_mapping",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_id_mapping_tenant_user_id",
        columnNames = {"tenant_id", "user_id"}
    )
)
@Data
@EqualsAndHashCode(callSuper = true)
public class UserIdMapping extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;
    
    @Column(name = "tenant_id")
    private Long tenantId;
    
    @Override
    public Long getId() {
        return internalId;
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