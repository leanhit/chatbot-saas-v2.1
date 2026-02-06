package com.chatbot.modules.address.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Entity
@Table(
    name = "addresses",
    indexes = {
        @Index(name = "idx_address_tenant", columnList = "tenant_id"),
        @Index(name = "idx_address_owner", columnList = "tenant_id, owner_type, owner_id")
    }
)
// --- THÊM PHẦN NÀY ---
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "tenantId", type = Long.class)
)
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
// ---------------------
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tenant sở hữu address
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /**
     * Loại owner: USER, END_USER, ORDER, SHOP...
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 30)
    private OwnerType ownerType;

    /**
     * ID của owner tương ứng với ownerType
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    // ===== Địa chỉ =====
    @Column(name = "house_number", length = 50)
    private String houseNumber;

    @Column(length = 255)
    private String street;

    @Column(length = 255)
    private String ward;

    @Column(length = 255)
    private String district;

    @Column(length = 255)
    private String province;

    @Column(length = 255)
    private String country;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
    
    // Thêm setter cho compatibility
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public boolean isDefault() {
        return this.isDefault;
    }
}