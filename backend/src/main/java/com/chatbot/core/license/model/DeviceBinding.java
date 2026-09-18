package com.chatbot.core.license.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "device_bindings", indexes = {
    @Index(name = "idx_device_binding_user", columnList = "user_id"),
    @Index(name = "idx_device_binding_device", columnList = "device_id"),
    @Index(name = "idx_device_binding_user_device", columnList = "user_id, device_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceBinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, REVOKED

    @Column(name = "activated_at")
    @Builder.Default
    private Instant activatedAt = Instant.now();

    @Column(name = "last_seen_at")
    @Builder.Default
    private Instant lastSeenAt = Instant.now();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
