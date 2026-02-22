package com.chatbot.shared.exceptions;

public class UnauthorizedException extends RuntimeException {

    private String reason;
    private String resource;
    private String action;

    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public UnauthorizedException(String message, String reason, String resource) {
        super(message);
        this.reason = reason;
        this.resource = resource;
    }

    public UnauthorizedException(String message, String reason, String resource, String action) {
        super(message);
        this.reason = reason;
        this.resource = resource;
        this.action = action;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean hasReason() {
        return reason != null && !reason.trim().isEmpty();
    }

    public boolean hasResource() {
        return resource != null && !resource.trim().isEmpty();
    }

    public boolean hasAction() {
        return action != null && !action.trim().isEmpty();
    }

    public static UnauthorizedException fromReason(String reason) {
        return new UnauthorizedException("Unauthorized", reason);
    }

    public static UnauthorizedException fromResource(String resource) {
        return new UnauthorizedException("Access denied to resource: " + resource, "RESOURCE_ACCESS_DENIED", resource);
    }

    public static UnauthorizedException fromAction(String action) {
        return new UnauthorizedException("Access denied for action: " + action, "ACTION_ACCESS_DENIED", null, action);
    }

    public static UnauthorizedException fromResourceAndAction(String resource, String action) {
        return new UnauthorizedException("Access denied for action: " + action + " on resource: " + resource, "RESOURCE_ACTION_ACCESS_DENIED", resource, action);
    }

    public static UnauthorizedException authenticationRequired() {
        return new UnauthorizedException("Authentication required", "AUTHENTICATION_REQUIRED");
    }

    public static UnauthorizedException tokenExpired() {
        return new UnauthorizedException("Token has expired", "TOKEN_EXPIRED");
    }

    public static UnauthorizedException tokenInvalid() {
        return new UnauthorizedException("Invalid token", "TOKEN_INVALID");
    }

    public static UnauthorizedException tokenMissing() {
        return new UnauthorizedException("Token is missing", "TOKEN_MISSING");
    }

    public static UnauthorizedException apiKeyInvalid() {
        return new UnauthorizedException("Invalid API key", "API_KEY_INVALID");
    }

    public static UnauthorizedException apiKeyMissing() {
        return new UnauthorizedException("API key is missing", "API_KEY_MISSING");
    }

    public static UnauthorizedException sessionExpired() {
        return new UnauthorizedException("Session has expired", "SESSION_EXPIRED");
    }

    public static UnauthorizedException sessionInvalid() {
        return new UnauthorizedException("Invalid session", "SESSION_INVALID");
    }

    public static UnauthorizedException accountLocked() {
        return new UnauthorizedException("Account is locked", "ACCOUNT_LOCKED");
    }

    public static UnauthorizedException accountSuspended() {
        return new UnauthorizedException("Account is suspended", "ACCOUNT_SUSPENDED");
    }

    public static UnauthorizedException accountInactive() {
        return new UnauthorizedException("Account is inactive", "ACCOUNT_INACTIVE");
    }

    public static UnauthorizedException emailNotVerified() {
        return new UnauthorizedException("Email address not verified", "EMAIL_NOT_VERIFIED");
    }

    public static UnauthorizedException insufficientPermissions() {
        return new UnauthorizedException("Insufficient permissions", "INSUFFICIENT_PERMISSIONS");
    }

    public static UnauthorizedException roleInsufficient() {
        return new UnauthorizedException("Insufficient role privileges", "ROLE_INSUFFICIENT");
    }

    public static UnauthorizedException tenantAccessDenied() {
        return new UnauthorizedException("Tenant access denied", "TENANT_ACCESS_DENIED");
    }

    public static UnauthorizedException ipBlocked() {
        return new UnauthorizedException("IP address is blocked", "IP_BLOCKED");
    }

    public static UnauthorizedException rateLimitExceeded() {
        return new UnauthorizedException("Rate limit exceeded", "RATE_LIMIT_EXCEEDED");
    }

    public static UnauthorizedException csrfTokenInvalid() {
        return new UnauthorizedException("Invalid CSRF token", "CSRF_TOKEN_INVALID");
    }

    public static UnauthorizedException csrfTokenMissing() {
        return new UnauthorizedException("CSRF token is missing", "CSRF_TOKEN_MISSING");
    }

    @Override
    public String toString() {
        return "UnauthorizedException{" +
                "message='" + getMessage() + '\'' +
                ", reason='" + reason + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
