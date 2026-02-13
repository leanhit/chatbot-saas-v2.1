package com.chatbot.core.app.registry.model;

import com.chatbot.shared.infrastructure.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "app_registry")
public class AppRegistry extends BaseEntity {
    
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
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_public")
    private Boolean isPublic = false;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.Set<AppConfiguration> configurations = new java.util.HashSet<>();
    
    // Constructors
    public AppRegistry() {}
    
    public AppRegistry(String name, String displayName, String description, AppType appType) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.appType = appType;
        this.status = AppStatus.DRAFT;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public AppType getAppType() {
        return appType;
    }
    
    public void setAppType(AppType appType) {
        this.appType = appType;
    }
    
    public AppStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppStatus status) {
        this.status = status;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getApiEndpoint() {
        return apiEndpoint;
    }
    
    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }
    
    public String getWebhookUrl() {
        return webhookUrl;
    }
    
    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
    
    public String getConfigSchema() {
        return configSchema;
    }
    
    public void setConfigSchema(String configSchema) {
        this.configSchema = configSchema;
    }
    
    public String getDefaultConfig() {
        return defaultConfig;
    }
    
    public void setDefaultConfig(String defaultConfig) {
        this.defaultConfig = defaultConfig;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public Long getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public java.util.Set<AppConfiguration> getConfigurations() {
        return configurations;
    }
    
    public void setConfigurations(java.util.Set<AppConfiguration> configurations) {
        this.configurations = configurations;
    }
}
