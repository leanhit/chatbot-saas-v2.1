package com.chatbot.shared.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ErrorResponse {

    private String status = "error";
    private String code;
    private String message;
    private String description;
    private List<String> errors;
    private Map<String, Object> details;
    private String path;
    private LocalDateTime timestamp;
    private String correlationId;
    private String requestId;
    private String userId;
    private String tenantId;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public ErrorResponse(String code, String message, String description) {
        this(code, message);
        this.description = description;
    }

    public ErrorResponse(String code, String message, List<String> errors) {
        this(code, message);
        this.errors = errors;
    }

    public ErrorResponse(String code, String message, String description, List<String> errors) {
        this(code, message, description);
        this.errors = errors;
    }

    public static ErrorResponse fromCode(String code, String message) {
        return new ErrorResponse(code, message);
    }

    public static ErrorResponse fromMessage(String message) {
        return new ErrorResponse(null, message);
    }

    public static ErrorResponse fromValidation(List<String> errors) {
        return new ErrorResponse("VALIDATION_ERROR", "Validation failed", errors);
    }

    public static ErrorResponse fromValidation(String field, String error) {
        return new ErrorResponse("VALIDATION_ERROR", "Validation failed", List.of(field + ": " + error));
    }

    public static ErrorResponse fromNotFound(String resource) {
        return new ErrorResponse("NOT_FOUND", resource + " not found");
    }

    public static ErrorResponse fromUnauthorized(String message) {
        return new ErrorResponse("UNAUTHORIZED", message);
    }

    public static ErrorResponse fromForbidden(String message) {
        return new ErrorResponse("FORBIDDEN", message);
    }

    public static ErrorResponse fromConflict(String message) {
        return new ErrorResponse("CONFLICT", message);
    }

    public static ErrorResponse fromInternalError(String message) {
        return new ErrorResponse("INTERNAL_ERROR", message);
    }

    public static ErrorResponse fromServiceUnavailable(String message) {
        return new ErrorResponse("SERVICE_UNAVAILABLE", message);
    }

    public static ErrorResponse fromTimeout(String message) {
        return new ErrorResponse("TIMEOUT", message);
    }

    public static ErrorResponse fromRateLimit(String message) {
        return new ErrorResponse("RATE_LIMIT_EXCEEDED", message);
    }

    public static ErrorResponse fromIntegration(String service, String message) {
        return new ErrorResponse("INTEGRATION_ERROR", service + ": " + message);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public boolean hasCode() {
        return code != null && !code.trim().isEmpty();
    }

    public boolean hasMessage() {
        return message != null && !message.trim().isEmpty();
    }

    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public boolean hasDetails() {
        return details != null && !details.isEmpty();
    }

    public boolean hasPath() {
        return path != null && !path.trim().isEmpty();
    }

    public boolean hasCorrelationId() {
        return correlationId != null && !correlationId.trim().isEmpty();
    }

    public boolean hasRequestId() {
        return requestId != null && !requestId.trim().isEmpty();
    }

    public boolean hasUserId() {
        return userId != null && !userId.trim().isEmpty();
    }

    public boolean hasTenantId() {
        return tenantId != null && !tenantId.trim().isEmpty();
    }

    public ErrorResponse withCode(String code) {
        this.code = code;
        return this;
    }

    public ErrorResponse withMessage(String message) {
        this.message = message;
        return this;
    }

    public ErrorResponse withDescription(String description) {
        this.description = description;
        return this;
    }

    public ErrorResponse withErrors(List<String> errors) {
        this.errors = errors;
        return this;
    }

    public ErrorResponse withError(String error) {
        if (this.errors == null) {
            this.errors = new java.util.ArrayList<>();
        }
        this.errors.add(error);
        return this;
    }

    public ErrorResponse withDetails(Map<String, Object> details) {
        this.details = details;
        return this;
    }

    public ErrorResponse withDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new java.util.HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }

    public ErrorResponse withPath(String path) {
        this.path = path;
        return this;
    }

    public ErrorResponse withCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public ErrorResponse withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public ErrorResponse withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public ErrorResponse withTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public ErrorResponse withTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ErrorResponse addError(String error) {
        if (this.errors == null) {
            this.errors = new java.util.ArrayList<>();
        }
        this.errors.add(error);
        return this;
    }

    public ErrorResponse addErrors(List<String> errors) {
        if (this.errors == null) {
            this.errors = new java.util.ArrayList<>();
        }
        this.errors.addAll(errors);
        return this;
    }

    public ErrorResponse addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new java.util.HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }

    public ErrorResponse addDetails(Map<String, Object> details) {
        if (this.details == null) {
            this.details = new java.util.HashMap<>();
        }
        this.details.putAll(details);
        return this;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "status='" + status + '\'' +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", description='" + description + '\'' +
                ", errors=" + errors +
                ", details=" + details +
                ", path='" + path + '\'' +
                ", timestamp=" + timestamp +
                ", correlationId='" + correlationId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", userId='" + userId + '\'' +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }
}
