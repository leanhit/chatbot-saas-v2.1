package com.chatbot.core.app.registry.dto;

import com.chatbot.core.app.registry.model.AppType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

public class RegisterAppRequest {
    
    @NotBlank(message = "App name is required")
    @Size(min = 3, max = 100, message = "App name must be between 3 and 100 characters")
    private String name;
    
    @NotBlank(message = "Display name is required")
    @Size(max = 200, message = "Display name must not exceed 200 characters")
    private String displayName;
    
    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "App type is required")
    private AppType appType;
    
    @Size(max = 50, message = "Version must not exceed 50 characters")
    private String version;
    
    @Size(max = 500, message = "API endpoint must not exceed 500 characters")
    private String apiEndpoint;
    
    @Size(max = 500, message = "Webhook URL must not exceed 500 characters")
    private String webhookUrl;
    
    private String configSchema;
    
    private String defaultConfig;
    
    private Boolean isPublic = false;
    
    private Map<String, Object> configurations;
    
    // Constructors
    public RegisterAppRequest() {}
    
    public RegisterAppRequest(String name, String displayName, String description, AppType appType) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.appType = appType;
    }
    
    // Getters and Setters
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
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public Map<String, Object> getConfigurations() {
        return configurations;
    }
    
    public void setConfigurations(Map<String, Object> configurations) {
        this.configurations = configurations;
    }
}
