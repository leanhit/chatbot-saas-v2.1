package com.chatbot.core.app.registry.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "app_registry")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class AppRegistry extends BaseTenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String displayName;
    
    @Column(nullable = false)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppType appType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppStatus status;
    
    @Column(name = "version")
    private String version;
    
    @Column(name = "api_endpoint")
    private String apiEndpoint;
    
    @Column(name = "webhook_url")
    private String webhookUrl;
    
    @Column(name = "config_schema")
    @Lob
    private String configSchema;
    
    @Column(name = "default_config")
    @Lob
    private String defaultConfig;
    
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Builder.Default
    @Column(name = "is_public")
    private Boolean isPublic = false;
    
    @Builder.Default
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Set<AppConfiguration> configurations = new java.util.HashSet<>();
    
    @Override
    public Object getId() {
        return id;
    }
    
    @Override
    public Long getTenantId() {
        return super.getTenantId();
    }
    
    @Override
    public void setTenantId(Long tenantId) {
        super.setTenantId(tenantId);
    }
    
    @PrePersist
    protected void onCreate() {
        super.onCreate();
    }
}
