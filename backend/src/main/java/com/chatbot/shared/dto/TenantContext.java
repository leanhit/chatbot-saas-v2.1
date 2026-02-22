package com.chatbot.shared.dto;

public class TenantContext {

    private static final ThreadLocal<TenantContext> contextHolder = new ThreadLocal<>();

    private Long tenantId;
    private String tenantName;
    private String tenantDomain;
    private String tenantStatus;
    private String userId;
    private String username;
    private String userRole;
    private String correlationId;
    private String requestId;
    private String ipAddress;
    private String userAgent;
    private String locale;
    private String timezone;

    public TenantContext() {
    }

    public TenantContext(Long tenantId) {
        this.tenantId = tenantId;
    }

    public TenantContext(Long tenantId, String userId) {
        this.tenantId = tenantId;
        this.userId = userId;
    }

    public static void setCurrentContext(TenantContext context) {
        contextHolder.set(context);
    }

    public static TenantContext getCurrentContext() {
        return contextHolder.get();
    }

    public static Long getCurrentTenantId() {
        TenantContext context = getCurrentContext();
        return context != null ? context.getTenantId() : null;
    }

    public static String getCurrentUserId() {
        TenantContext context = getCurrentContext();
        return context != null ? context.getUserId() : null;
    }

    public static String getCurrentUsername() {
        TenantContext context = getCurrentContext();
        return context != null ? context.getUsername() : null;
    }

    public static String getCurrentUserRole() {
        TenantContext context = getCurrentContext();
        return context != null ? context.getUserRole() : null;
    }

    public static String getCurrentCorrelationId() {
        TenantContext context = getCurrentContext();
        return context != null ? context.getCorrelationId() : null;
    }

    public static void clear() {
        contextHolder.remove();
    }

    public static boolean hasContext() {
        return getCurrentContext() != null;
    }

    public static boolean hasTenantId() {
        return getCurrentTenantId() != null;
    }

    public static boolean hasUserId() {
        return getCurrentUserId() != null;
    }

    public static TenantContext createEmpty() {
        return new TenantContext();
    }

    public static TenantContext fromTenantId(Long tenantId) {
        return new TenantContext(tenantId);
    }

    public static TenantContext fromTenantAndUser(Long tenantId, String userId) {
        return new TenantContext(tenantId, userId);
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

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getTenantStatus() {
        return tenantStatus;
    }

    public void setTenantStatus(String tenantStatus) {
        this.tenantStatus = tenantStatus;
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

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
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

    public boolean hasUsername() {
        return username != null && username.trim().length() > 0;
    }

    public boolean hasUserRole() {
        return userRole != null && userRole.trim().length() > 0;
    }

    public boolean hasCorrelationId() {
        return correlationId != null && !correlationId.trim().isEmpty();
    }

    public boolean hasRequestId() {
        return requestId != null && !requestId.trim().isEmpty();
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

    public TenantContext copy() {
        TenantContext copy = new TenantContext();
        copy.tenantId = this.tenantId;
        copy.tenantName = this.tenantName;
        copy.tenantDomain = this.tenantDomain;
        copy.tenantStatus = this.tenantStatus;
        copy.userId = this.userId;
        copy.username = this.username;
        copy.userRole = this.userRole;
        copy.correlationId = this.correlationId;
        copy.requestId = this.requestId;
        copy.ipAddress = this.ipAddress;
        copy.userAgent = this.userAgent;
        copy.locale = this.locale;
        copy.timezone = this.timezone;
        return copy;
    }

    public TenantContext withTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public TenantContext withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public TenantContext withUsername(String username) {
        this.username = username;
        return this;
    }

    public TenantContext withUserRole(String userRole) {
        this.userRole = userRole;
        return this;
    }

    public TenantContext withCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public TenantContext withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public TenantContext withIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public TenantContext withUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public TenantContext withLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public TenantContext withTimezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    @Override
    public String toString() {
        return "TenantContext{" +
                "tenantId=" + tenantId +
                ", tenantName='" + tenantName + '\'' +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", tenantStatus='" + tenantStatus + '\'' +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", userRole='" + userRole + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", locale='" + locale + '\'' +
                ", timezone='" + timezone + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantContext that = (TenantContext) o;
        return tenantId != null ? tenantId.equals(that.tenantId) : that.tenantId == null &&
               userId != null ? userId.equals(that.userId) : that.userId == null;
    }

    @Override
    public int hashCode() {
        int result = tenantId != null ? tenantId.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }
}
