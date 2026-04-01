package com.chatbot.core.simplepayment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "package_upgrade_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PackageUpgradeAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "payment_reference_code", nullable = false)
    private String paymentReferenceCode;

    @Column(name = "from_package_id")
    private String fromPackageId;

    @Column(name = "to_package_id", nullable = false)
    private String toPackageId;

    @Column(name = "payment_amount", nullable = false, precision = 15, scale = 2)
    private java.math.BigDecimal paymentAmount;

    @Column(name = "currency", nullable = false)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "bank_transaction_id")
    private String bankTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "upgrade_status", nullable = false)
    private UpgradeStatus upgradeStatus;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum UpgradeStatus {
        SUCCESS,
        FAILED,
        PENDING
    }

    @PrePersist
    protected void onCreate() {
        if (upgradeStatus == null) {
            upgradeStatus = UpgradeStatus.PENDING;
        }
    }
}
