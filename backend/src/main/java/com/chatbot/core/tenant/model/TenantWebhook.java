package com.chatbot.core.tenant.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.chatbot.shared.utils.DateUtils;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Tenant-configured outbound webhook endpoint for conversation events.
 * events = comma-separated event types, or "*" for all events.
 */
@Entity
@Table(name = "tenant_webhooks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantWebhook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @Column(name = "secret")
    private String secret;

    @Column(name = "events", columnDefinition = "TEXT")
    private String events;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @CreationTimestamp
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean subscribesTo(String eventType) {
        if (events == null || events.isBlank() || "*".equals(events.trim())) {
            return true;
        }
        for (String e : events.split(",")) {
            if (e.trim().equals(eventType)) {
                return true;
            }
        }
        return false;
    }
}
