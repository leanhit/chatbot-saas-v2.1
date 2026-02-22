package com.chatbot.shared.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UserContext {

    private static final ThreadLocal<UserContext> contextHolder = new ThreadLocal<>();

    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private List<String> roles;
    private List<String> permissions;
    private Map<String, Object> attributes;
    private Long tenantId;
    private String tenantName;
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private String locale;
    private String timezone;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    private String status;
    private String correlationId;
    private String requestId;

    public UserContext() {
    }

    public UserContext(String userId) {
        this.userId = userId;
    }

    public UserContext(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public static void setCurrentContext(UserContext context) {
        contextHolder.set(context);
    }

    public static UserContext getCurrentContext() {
        return contextHolder.get();
    }

    public static String getCurrentUserId() {
        UserContext context = getCurrentContext();
        return context != null ? context.getUserId() : null;
    }

    public static String getCurrentUsername() {
        UserContext context = getCurrentContext();
        return context != null ? context.getUsername() : null;
    }

    public static String getCurrentEmail() {
        UserContext context = getCurrentContext();
        return context != null ? context.getEmail() : null;
    }

    public static List<String> getCurrentRoles() {
        UserContext context = getCurrentContext();
        return context != null ? context.getRoles() : null;
    }

    public static List<String> getCurrentPermissions() {
        UserContext context = getCurrentContext();
        return context != null ? context.getPermissions() : null;
    }

    public static Long getCurrentTenantId() {
        UserContext context = getCurrentContext();
        return context != null ? context.getTenantId() : null;
    }

    public static boolean hasRole(String role) {
        List<String> roles = getCurrentRoles();
        return roles != null && roles.contains(role);
    }

    public static boolean hasPermission(String permission) {
        List<String> permissions = getCurrentPermissions();
        return permissions != null && permissions.contains(permission);
    }

    public static boolean hasAnyRole(String... roles) {
        List<String> currentRoles = getCurrentRoles();
        if (currentRoles == null) return false;
        
        for (String role : roles) {
            if (currentRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAllRoles(String... roles) {
        List<String> currentRoles = getCurrentRoles();
        if (currentRoles == null) return false;
        
        for (String role : roles) {
            if (!currentRoles.contains(role)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasAnyPermission(String... permissions) {
        List<String> currentPermissions = getCurrentPermissions();
        if (currentPermissions == null) return false;
        
        for (String permission : permissions) {
            if (currentPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAllPermissions(String... permissions) {
        List<String> currentPermissions = getCurrentPermissions();
        if (currentPermissions == null) return false;
        
        for (String permission : permissions) {
            if (!currentPermissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }

    public static void clear() {
        contextHolder.remove();
    }

    public static boolean hasContext() {
        return getCurrentContext() != null;
    }

    public static boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }

    public static UserContext createEmpty() {
        return new UserContext();
    }

    public static UserContext fromUserId(String userId) {
        return new UserContext(userId);
    }

    public static UserContext fromUserAndTenant(String userId, Long tenantId) {
        UserContext context = new UserContext(userId);
        context.setTenantId(tenantId);
        return context;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean hasUserId() {
        return userId != null && !userId.trim().isEmpty();
    }

    public boolean hasUsername() {
        return username != null && !username.trim().isEmpty();
    }

    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }

    public boolean hasRoles() {
        return roles != null && !roles.isEmpty();
    }

    public boolean hasPermissions() {
        return permissions != null && !permissions.isEmpty();
    }

    public boolean hasAttributes() {
        return attributes != null && !attributes.isEmpty();
    }

    public boolean hasTenantId() {
        return tenantId != null;
    }

    public boolean hasSessionId() {
        return sessionId != null && !sessionId.trim().isEmpty();
    }

    public boolean hasIpAddress() {
        return ipAddress != null && !ipAddress.trim().isEmpty();
    }

    public boolean hasUserAgent() {
        return userAgent != null && !userAgent.trim().isEmpty();
    }

    public boolean hasLocale() {
        return locale != null && !locale.trim().isEmpty();
    }

    public boolean hasTimezone() {
        return timezone != null && !timezone.trim().isEmpty();
    }

    public boolean isActive() {
        return active != null && active;
    }

    public void addRole(String role) {
        if (this.roles == null) {
            this.roles = new java.util.ArrayList<>();
        }
        if (!this.roles.contains(role)) {
            this.roles.add(role);
        }
    }

    public void removeRole(String role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }

    public void addPermission(String permission) {
        if (this.permissions == null) {
            this.permissions = new java.util.ArrayList<>();
        }
        if (!this.permissions.contains(permission)) {
            this.permissions.add(permission);
        }
    }

    public void removePermission(String permission) {
        if (this.permissions != null) {
            this.permissions.remove(permission);
        }
    }

    public void setAttribute(String key, Object value) {
        if (this.attributes == null) {
            this.attributes = new java.util.HashMap<>();
        }
        this.attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }

    public void removeAttribute(String key) {
        if (attributes != null) {
            attributes.remove(key);
        }
    }

    public UserContext copy() {
        UserContext copy = new UserContext();
        copy.userId = this.userId;
        copy.username = this.username;
        copy.email = this.email;
        copy.firstName = this.firstName;
        copy.lastName = this.lastName;
        copy.fullName = this.fullName;
        copy.roles = this.roles != null ? new java.util.ArrayList<>(this.roles) : null;
        copy.permissions = this.permissions != null ? new java.util.ArrayList<>(this.permissions) : null;
        copy.attributes = this.attributes != null ? new java.util.HashMap<>(this.attributes) : null;
        copy.tenantId = this.tenantId;
        copy.tenantName = this.tenantName;
        copy.sessionId = this.sessionId;
        copy.ipAddress = this.ipAddress;
        copy.userAgent = this.userAgent;
        copy.locale = this.locale;
        copy.timezone = this.timezone;
        copy.lastLoginAt = this.lastLoginAt;
        copy.createdAt = this.createdAt;
        copy.updatedAt = this.updatedAt;
        copy.active = this.active;
        copy.status = this.status;
        copy.correlationId = this.correlationId;
        copy.requestId = this.requestId;
        return copy;
    }

    public UserContext withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public UserContext withUsername(String username) {
        this.username = username;
        return this;
    }

    public UserContext withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserContext withTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public UserContext withRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public UserContext withPermissions(List<String> permissions) {
        this.permissions = permissions;
        return this;
    }

    public UserContext withCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    @Override
    public String toString() {
        return "UserContext{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", roles=" + roles +
                ", permissions=" + permissions +
                ", tenantId=" + tenantId +
                ", tenantName='" + tenantName + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", locale='" + locale + '\'' +
                ", timezone='" + timezone + '\'' +
                ", active=" + active +
                ", status='" + status + '\'' +
                ", correlationId='" + correlationId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserContext that = (UserContext) o;
        return userId != null ? userId.equals(that.userId) : that.userId == null;
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}
