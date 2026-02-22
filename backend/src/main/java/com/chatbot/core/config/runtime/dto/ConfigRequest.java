package com.chatbot.core.config.runtime.dto;

import com.chatbot.core.config.runtime.model.ConfigScope;
import com.chatbot.core.config.runtime.model.ConfigType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfigRequest {
    
    @NotBlank(message = "Config key is required")
    private String configKey;
    
    private String configValue;
    
    private String defaultValue;
    
    @NotNull(message = "Config type is required")
    private ConfigType configType;
    
    @NotNull(message = "Config scope is required")
    private ConfigScope configScope;
    
    private Long tenantId;
    
    private Long userId;
    
    private Boolean isEncrypted = false;
    
    private Boolean isReadonly = false;
    
    private String description;
}
