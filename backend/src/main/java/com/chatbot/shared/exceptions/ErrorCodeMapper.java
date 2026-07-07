package com.chatbot.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Utility mapper class to map ErrorCode enum to Spring HttpStatus and vice-versa.
 * Extracted from GlobalExceptionHandler to keep the exception handler clean and maintainable.
 */
public final class ErrorCodeMapper {

    private ErrorCodeMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Map ErrorCode to appropriate HTTP status.
     *
     * @param errorCode the error code
     * @return the mapped HttpStatus
     */
    public static HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        switch (errorCode) {
            case NOT_FOUND:
            case RESOURCE_NOT_FOUND:
            case USER_NOT_FOUND:
            case TENANT_NOT_FOUND:
            case LICENSE_NOT_FOUND:
            case PAYMENT_NOT_FOUND:
            case CONVERSATION_NOT_FOUND:
            case CONFIG_NOT_FOUND:
            case INVITATION_NOT_FOUND:
            case JOIN_REQUEST_NOT_FOUND:
            case CONNECTION_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
                
            case VALIDATION_ERROR:
            case BAD_REQUEST:
            case INVALID_REQUEST_BODY:
            case INVALID_TENANT_KEY:
            case INVALID_PAYMENT_AMOUNT:
            case INVALID_STATUS_TRANSITION:
            case INVALID_JOIN_REQUEST:
            case INVITATION_INVALID:
            case CONVERSATION_IDS_REQUIRED:
            case TENANT_ID_REQUIRED:
                return HttpStatus.BAD_REQUEST;
                
            case UNAUTHORIZED:
            case BAD_CREDENTIALS:
            case AUTHENTICATION_FAILED:
            case INVALID_TOKEN:
            case LICENSE_EXPIRED:
            case LICENSE_INACTIVE:
            case TOKEN_INVALID_OR_EXPIRED:
            case REFRESH_TOKEN_EXPIRED:
            case USER_NOT_AUTHENTICATED:
                return HttpStatus.UNAUTHORIZED;
                
            case FORBIDDEN:
            case INSUFFICIENT_PERMISSION:
            case TENANT_INACTIVE:
            case NOT_TENANT_MEMBER:
            case NOT_TENANT_MEMBER_SELF:
            case NOT_CONVERSATION_MEMBER:
            case CANNOT_ACCESS_TENANT:
            case CANNOT_MANAGE_MEMBERS:
            case CANNOT_VIEW_JOIN_REQUESTS:
            case CANNOT_APPROVE_JOIN_REQUESTS:
            case CANNOT_ASSIGN_CONVERSATION:
            case CANNOT_RELEASE_CONVERSATION:
            case CANNOT_TAKEOVER_CONVERSATION:
            case INVITATION_PERMISSION_DENIED:
            case CANNOT_ACCEPT_INVITATION:
            case CANNOT_REJECT_INVITATION:
            case CANNOT_REVOKE_INVITATION:
            case CANNOT_SUSPEND_TENANT:
            case CANNOT_RESUME_TENANT:
            case CANNOT_DELETE_TENANT:
            case CANNOT_UPLOAD_LOGO:
            case CANNOT_UPDATE_LOGO:
            case CANNOT_UPLOAD_AVATAR:
            case CANNOT_CREATE_AVATAR_CATEGORY:
            case CANNOT_DELETE_CONVERSATIONS:
                return HttpStatus.FORBIDDEN;
                
            case CONFLICT:
            case DATA_INTEGRITY_VIOLATION:
            case OPTIMISTIC_LOCK:
            case EMAIL_ALREADY_EXISTS:
            case ALREADY_MEMBER:
            case JOIN_REQUEST_ALREADY_SENT:
            case INVITATION_ALREADY_PENDING:
            case TENANT_STATUS_TRANSITION:
            case TENANT_PROFILE_ERROR:
                return HttpStatus.CONFLICT;
                
            case PAYLOAD_TOO_LARGE:
                return HttpStatus.PAYLOAD_TOO_LARGE;
                
            case UNSUPPORTED_MEDIA_TYPE:
                return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
                
            case METHOD_NOT_ALLOWED:
                return HttpStatus.METHOD_NOT_ALLOWED;
                
            case TIMEOUT:
                return HttpStatus.REQUEST_TIMEOUT;
                
            case SERVICE_UNAVAILABLE:
            case BANK_API_ERROR:
                return HttpStatus.SERVICE_UNAVAILABLE;
                
            case RATE_LIMIT_EXCEEDED:
                return HttpStatus.TOO_MANY_REQUESTS;
                
            case ENDPOINT_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
                
            case INTEGRATION_ERROR:
            case INTERNAL_ERROR:
            case RUNTIME_ERROR:
            case PAYMENT_ERROR:
            case NOTIFICATION_ERROR:
            case BULK_DELETE_ERROR:
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * Map HTTP status code to appropriate ErrorCode.
     *
     * @param status the HTTP status code
     * @return the mapped ErrorCode
     */
    public static ErrorCode mapHttpStatusToErrorCode(HttpStatusCode status) {
        if (status == null) {
            return ErrorCode.INTERNAL_ERROR;
        }
        int value = status.value();
        switch (value) {
            case 404:
                return ErrorCode.NOT_FOUND;
            case 401:
                return ErrorCode.UNAUTHORIZED;
            case 403:
                return ErrorCode.FORBIDDEN;
            case 409:
                return ErrorCode.CONFLICT;
            case 429:
                return ErrorCode.RATE_LIMIT_EXCEEDED;
            default:
                return ErrorCode.BAD_REQUEST;
        }
    }
}
