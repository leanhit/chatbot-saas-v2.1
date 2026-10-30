package com.chatbot.core.customer.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Customer Profile entity for managing customer information and analytics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customer_profiles")
@EqualsAndHashCode(callSuper = true)
public class CustomerProfile extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column
    private String email;

    @Column
    private String phone;

    @Column
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private CustomerStatus status;

    @Enumerated(EnumType.STRING)
    private CustomerTier tier;

    @Column(name = "first_contact_at")
    private LocalDateTime firstContactAt;

    @Column(name = "last_contact_at")
    private LocalDateTime lastContactAt;

    @Column(name = "total_conversations")
    private Integer totalConversations;

    @Column(name = "total_messages")
    private Integer totalMessages;

    @Column(name = "avg_response_time_seconds")
    private Integer avgResponseTimeSeconds;

    @Column(name = "satisfaction_score")
    private Double satisfactionScore;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> customAttributes;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> tags;

    @Column(name = "preferred_language")
    private String preferredLanguage;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "is_blocked")
    private Boolean isBlocked;

    @Column(name = "blocked_reason")
    private String blockedReason;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Override
    public Long getId() {
        return id;
    }

    public enum CustomerStatus {
        ACTIVE, INACTIVE, BLOCKED, CHURNED
    }

    public enum CustomerTier {
        FREE, BASIC, PREMIUM, ENTERPRISE
    }
}
