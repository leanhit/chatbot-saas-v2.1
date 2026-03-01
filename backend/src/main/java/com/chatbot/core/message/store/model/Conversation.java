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
@Table(name = "conversation",
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
    private Boolean isClosedByAgent; // Mặc định là false

    /**
     * True nếu Agent đã tiếp quản conversation và Botpress nên im lặng (Bot Flow bị ngắt).
     * False nếu Conversation đang được Botpress xử lý (Bot Flow đang hoạt động).
     */
    @Column(nullable = false)
    private Boolean isTakenOverByAgent; // Mặc định là Botpress đang hoạt động

    @JdbcTypeCode(SqlTypes.JSON)
    private String tags; // Có thể null

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