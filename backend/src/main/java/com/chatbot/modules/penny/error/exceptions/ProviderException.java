package com.chatbot.modules.penny.error.exceptions;

/**
 * Exception for provider-related errors
 */
public class ProviderException extends PennyException {
    
    private final String providerType;
    
    public ProviderException(String message, String providerType) {
        super(message, "PROVIDER_ERROR");
        this.providerType = providerType;
    }
    
    public ProviderException(String message, String providerType, Throwable cause) {
        super(message, "PROVIDER_ERROR", cause);
        this.providerType = providerType;
    }
    
    public String getProviderType() {
        return providerType;
    }
}
