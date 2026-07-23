package com.chatbot.shared.exceptions;

/**
 * Exception for agent validation errors.
 * Used when agent configuration fails validation checks.
 */
public class AgentValidationException extends BaseException {
    
    public AgentValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public AgentValidationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    // Convenience methods for common agent validation errors
    
    public static AgentValidationException nameEmpty() {
        return new AgentValidationException(ErrorCode.INVALID_AGENT_NAME, 
            "Agent name cannot be empty");
    }
    
    public static AgentValidationException emailEmpty() {
        return new AgentValidationException(ErrorCode.INVALID_AGENT_EMAIL, 
            "Agent email cannot be empty");
    }
    
    public static AgentValidationException invalidEmail(String email) {
        return new AgentValidationException(ErrorCode.INVALID_AGENT_EMAIL, 
            "Invalid email format: " + email);
    }
    
    public static AgentValidationException roleNull() {
        return new AgentValidationException(ErrorCode.INVALID_AGENT_ROLE, 
            "Agent role cannot be null");
    }
    
    public static AgentValidationException invalidMaxConcurrent(int value) {
        return new AgentValidationException(ErrorCode.INVALID_AGENT_MAX_CONCURRENT, 
            "Max concurrent conversations must be greater than 0, got: " + value);
    }
    
    public static AgentValidationException invalidCurrentLoad(int value, int max) {
        return new AgentValidationException(ErrorCode.INVALID_AGENT_CURRENT_LOAD, 
            "Current load cannot be negative or exceed max concurrent. Current: " + value + ", Max: " + max);
    }
    
    public static AgentValidationException tenantIdRequired() {
        return new AgentValidationException(ErrorCode.AGENT_TENANT_ID_REQUIRED, 
            "Tenant ID cannot be null");
    }
}
