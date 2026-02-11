package com.chatbot.modules.facebook.connection.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "facebook_connection",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_facebook_connection_tenant_page",
        columnNames = {"tenant_id", "page_id"}
    )
)
@EqualsAndHashCode(callSuper = true)
public class FacebookConnection extends BaseTenantEntity {
    
    @Id
    private UUID id;
    private String botId;
    private String botName;
    private String ownerId;
    @Column(name = "page_id")
    private String pageId;
    private String fanpageUrl;
    private String pageAccessToken;
    private String fbUserId;
    private boolean isEnabled; // Trường mới
    private boolean isActive;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "chatbot_provider")
    private ChatbotProvider chatbotProvider = ChatbotProvider.BOTPRESS; // Mặc định là BOTPRESS

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime updatedAt; // Trường mới
    
    @Column(name = "tenant_id")
    private Long tenantId;
    
    @Override
    public UUID getId() {
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