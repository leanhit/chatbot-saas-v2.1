package com.chatbot.core.message.store.model;

import com.chatbot.core.message.store.model.Channel;
import com.chatbot.core.tenant.infra.BaseTenantEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.chatbot.shared.utils.DateUtils;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "conversations",
    indexes = {
        @Index(name = "idx_conversation_tenant", columnList = "tenant_id"),
        @Index(name = "idx_conversation_tenant_connection", columnList = "tenant_id, connection_id"),
        @Index(name = "idx_conversation_tenant_external_user", columnList = "tenant_id, external_user_id")
    }
)
@EqualsAndHashCode(callSuper = true)
public class Conversation extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Các trường đã có
    @Column(name = "connection_id")
    private UUID connectionId;

    private String ownerId;

    @Column(name = "external_user_id")
    private String externalUserId;

    // Thông tin người dùng
    private String userName;
    private String userAvatar;

    private String status; // open | closed

    // Các trường mới được thêm
    @Enumerated(EnumType.STRING) 
    private Channel channel;    

    private Long lastMessageId; // Có thể null khi mới tạo

    private Long agentAssignedId; // Có thể null khi chưa phân công

    @Column(nullable = false)
    @Builder.Default
    private Boolean isClosedByAgent = false; // Mặc định là false

    /**
     * True nếu Agent đã tiếp quản conversation và Botpress nên im lặng (Bot Flow bị ngắt).
     * False nếu Conversation đang được Botpress xử lý (Bot Flow đang hoạt động).
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isTakenOverByAgent = false; // Mặc định là Botpress đang hoạt động

    @JdbcTypeCode(SqlTypes.JSON)
    private String tags; // Có thể null

    // Attribute-based routing fields
    private String customerTier; // VIP, Enterprise, Standard
    private String language; // en, vi, etc.

    @JdbcTypeCode(SqlTypes.JSON)
    private String customAttributes; // JSON string for custom attributes

    // SLA Monitoring fields
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime firstAgentResponseTime;

    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime firstBotResponseTime;

    @Builder.Default
    private Integer slaBreachCount = 0;

    private Long expectedResponseTime; // in seconds

    // Skills-based routing fields (Phase 3.2)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_skills")
    private String requiredSkills; // JSON array of required skill strings, e.g. ["billing","technical"]

    // Multi-tier Escalation tracking fields (Phase 2.2)
    @Column(name = "current_escalation_tier")
    private Integer currentEscalationTier; // null = not escalated, 1/2/3 = current tier level

    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    @Column(name = "last_escalated_at")
    private LocalDateTime lastEscalatedAt; // timestamp of last escalation

    // Timestamp
    @CreationTimestamp
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime updatedAt;
    
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