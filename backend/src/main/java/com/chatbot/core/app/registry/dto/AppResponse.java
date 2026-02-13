package com.chatbot.core.app.registry.dto;

import com.chatbot.core.app.registry.model.AppType;
import com.chatbot.core.app.registry.model.AppStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AppResponse {
    
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private AppType appType;
    private AppStatus status;
    private String version;
    private String apiEndpoint;
    private String webhookUrl;
    private Boolean isActive;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private List<AppConfigurationDto> configurations;
    
    // Constructors
    public AppResponse() {}
    
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
    
    public List<AppConfigurationDto> getConfigurations() {
        return configurations;
    }
    
    public void setConfigurations(List<AppConfigurationDto> configurations) {
        this.configurations = configurations;
    }
    
    public static class AppConfigurationDto {
        private Long id;
        private String configKey;
        private String configValue;
        private String configType;
        private Boolean isRequired;
        private Boolean isEncrypted;
        private String description;
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getConfigKey() {
            return configKey;
        }
        
        public void setConfigKey(String configKey) {
            this.configKey = configKey;
        }
        
        public String getConfigValue() {
            return configValue;
        }
        
        public void setConfigValue(String configValue) {
            this.configValue = configValue;
        }
        
        public String getConfigType() {
            return configType;
        }
        
        public void setConfigType(String configType) {
            this.configType = configType;
        }
        
        public Boolean getIsRequired() {
            return isRequired;
        }
        
        public void setIsRequired(Boolean isRequired) {
            this.isRequired = isRequired;
        }
        
        public Boolean getIsEncrypted() {
            return isEncrypted;
        }
        
        public void setIsEncrypted(Boolean isEncrypted) {
            this.isEncrypted = isEncrypted;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
}
