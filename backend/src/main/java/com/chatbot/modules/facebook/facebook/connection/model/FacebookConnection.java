package com.chatbot.modules.facebook.facebook.connection.model;

import lombok.Data;

import jakarta.persistence.*;
import java.util.UUID;

@Data
@Entity
@Table(name = "facebook_connections")
public class FacebookConnection {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id")
    private UUID tenantId;
    
    @Column(name = "page_id")
    private String pageId;
    
    @Column(name = "page_access_token")
    private String pageAccessToken;
    
    @Column(name = "enabled")
    private boolean enabled;
    
    public boolean isEnabled() {
        return enabled;
    }
}
