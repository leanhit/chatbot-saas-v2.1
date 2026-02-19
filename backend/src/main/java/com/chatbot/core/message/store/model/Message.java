package com.chatbot.core.message.store.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "message",
    indexes = {
        @Index(name = "idx_message_tenant", columnList = "tenant_id"),
        @Index(name = "idx_message_tenant_conversation", columnList = "tenant_id, conversation_id")
    }
)
@EqualsAndHashCode(callSuper = true)
public class Message extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Các trường đã có
    private Long conversationId;

    private String sender; // user | bot

    @Column(columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    private String rawPayload;

    // Các trường mới được thêm

    @Column(nullable = false)
    private String messageType; // text | image | video | postback

    private String externalMessageId; // Có thể null

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false; // Mặc định là false (chưa đọc)

    private LocalDateTime sentTime; // Thời gian gốc trên nền tảng, có thể null

    // Timestamp
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
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