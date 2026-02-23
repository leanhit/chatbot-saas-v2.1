package com.chatbot.spokes.facebook.connection.model;

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
    @Column(name = "bot_id")
    private String botId;
    @Column(name = "bot_name")
    private String botName;
    @Column(name = "owner_id")
    private String ownerId;
    @Column(name = "page_id")
    private String pageId;
    @Column(name = "fanpage_url")
    private String fanpageUrl;
    @Column(name = "page_access_token")
    private String pageAccessToken;
    
    // Getters and setters for Lombok compatibility
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    @Column(name = "fb_user_id")
    private String fbUserId;
    @Column(name = "is_enabled")
    private boolean isEnabled; // Trường mới
    @Column(name = "is_active")
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