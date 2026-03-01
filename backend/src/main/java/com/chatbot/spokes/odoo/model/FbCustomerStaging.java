package com.chatbot.spokes.odoo.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.chatbot.shared.utils.DateUtils;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fb_customer_staging")
@EqualsAndHashCode(callSuper = true)
public class FbCustomerStaging extends BaseTenantEntity {

    @Id
    @Column(name = "psid", nullable = false)
    private String psid;

    @Column(name = "owner_id")
    private String ownerId;

    @Column(name = "page_id")
    private String pageId;

    @Column(name = "captured_phones", columnDefinition = "TEXT")
    private String phones;

    @Column(name = "data_json")
    private String dataJson;

    // Getters and setters for Lombok compatibility
    public String getPhones() { return phones; }
    public void setPhones(String phones) { this.phones = phones; }
    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private CustomerStatus status;

    @Column(name = "odoo_id")
    private Integer odooId;

    // ✅ Thêm 2 cột thời gian
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime updatedAt;

    // ✅ Constructor tiện dụng
    public FbCustomerStaging(String psid) {
        this.psid = psid;
        this.status = CustomerStatus.PENDING;
        this.dataJson = "{}";
    }
    
    // Getters and setters for Lombok compatibility
    public String getPageId() { return pageId; }
    public void setPageId(String pageId) { this.pageId = pageId; }
    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    
    @Column(name = "tenant_id")
    private Long tenantId;
    
    @Override
    public String getId() {
        return psid;
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
