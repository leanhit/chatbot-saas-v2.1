package com.chatbot.core.identity.service;

import com.chatbot.core.identity.constants.IdentityConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AuditService {
    
    /**
     * Log authentication events
     */
    public void logAuthEvent(String eventType, String email, String details, boolean success) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("eventType", eventType);
        auditData.put("email", email);
        auditData.put("details", details);
        auditData.put("success", success);
        auditData.put("timestamp", LocalDateTime.now());
        auditData.put("ipAddress", getCurrentUserIP());
        
        log.info("AUDIT: {}", auditData);
        
        // Here you could also save to database or send to external system
        // For now, we're using structured logging
    }
    
    /**
     * Log successful login
     */
    public void logLoginSuccess(String email) {
        logAuthEvent(IdentityConstants.AUDIT_LOGIN_SUCCESS, email, "User logged in successfully", true);
    }
    
    /**
     * Log failed login
     */
    public void logLoginFailure(String email, String reason) {
        logAuthEvent(IdentityConstants.AUDIT_LOGIN_FAILED, email, reason, false);
    }
    
    /**
     * Log successful registration
     */
    public void logRegistrationSuccess(String email) {
        logAuthEvent(IdentityConstants.AUDIT_REGISTER_SUCCESS, email, "User registered successfully", true);
    }
    
    /**
     * Log failed registration
     */
    public void logRegistrationFailure(String email, String reason) {
        logAuthEvent("REGISTRATION_FAILED", email, reason, false);
    }
    
    /**
     * Log password change
     */
    public void logPasswordChange(String email) {
        logAuthEvent(IdentityConstants.AUDIT_PASSWORD_CHANGED, email, "User changed password", true);
    }
    
    /**
     * Log role change
     */
    public void logRoleChange(String adminEmail, String targetEmail, String oldRole, String newRole) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("eventType", IdentityConstants.AUDIT_ROLE_CHANGED);
        auditData.put("adminEmail", adminEmail);
        auditData.put("targetEmail", targetEmail);
        auditData.put("oldRole", oldRole);
        auditData.put("newRole", newRole);
        auditData.put("timestamp", LocalDateTime.now());
        auditData.put("ipAddress", getCurrentUserIP());
        
        log.info("AUDIT: {}", auditData);
    }
    
    /**
     * Log token revocation
     */
    public void logTokenRevocation(String email, String reason) {
        logAuthEvent(IdentityConstants.AUDIT_TOKEN_REVOKED, email, reason, true);
    }
    
    /**
     * Log security event
     */
    public void logSecurityEvent(String eventType, String email, String details) {
        logAuthEvent(eventType, email, details, false);
    }
    
    /**
     * Get current user IP address
     * In a real implementation, this would come from the request context
     */
    private String getCurrentUserIP() {
        try {
            // This would typically come from HttpServletRequest
            // For now, returning a placeholder
            return "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}
