package com.chatbot.shared.exceptions;

import com.chatbot.shared.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler that handles all exceptions in the application.
 * This is the single source of truth for error response formatting.
 * All custom exceptions should extend BaseException and use ErrorCode enum.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    private String getCleanPath(WebRequest request) {
        if (request == null) {
            return "";
        }
        return request.getDescription(false).replace("uri=", "");
    }

    // Handle all BaseException subclasses (custom exceptions)
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        ErrorCode errorCode = ex.getErrorCode();
        String code = errorCode != null ? errorCode.getCode() : "INTERNAL_ERROR";
        
        ErrorResponse errorResponse = new ErrorResponse(code, ex.getMessage())
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        // Handle ValidationException with errors list
        if (ex instanceof ValidationException) {
            ValidationException validationEx = (ValidationException) ex;
            errorResponse = ErrorResponse.fromValidation(validationEx.getErrors())
                    .withCode(code)
                    .withPath(path)
                    .withTimestamp(java.time.LocalDateTime.now());
        }
        
        if (ex.getDetails() != null) {
            ex.getDetails().forEach(errorResponse::withDetail);
        }
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("BaseException [{}]: {} at path: {}", code, ex.getMessage(), path);
        
        HttpStatus status = ErrorCodeMapper.mapErrorCodeToHttpStatus(errorCode);
        return new ResponseEntity<>(errorResponse, status);
    }

    // Handle validation errors from @Valid annotation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.fromValidation(errors)
                .withCode(ErrorCode.VALIDATION_ERROR.getCode())
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("MethodArgumentNotValidException at path: {}. Errors: {}", path, errors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        List<String> errors = ex.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.fromValidation(errors)
                .withCode(ErrorCode.VALIDATION_ERROR.getCode())
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("BindException at path: {}. Errors: {}", path, errors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.fromValidation(errors)
                .withCode(ErrorCode.VALIDATION_ERROR.getCode())
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("ConstraintViolationException at path: {}. Violations: {}", path, errors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.BAD_REQUEST.getCode(), ex.getMessage())
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("IllegalArgumentException: {} at path: {}", ex.getMessage(), path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.CONFLICT.getCode(), ex.getMessage())
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("IllegalStateException: {} at path: {}", ex.getMessage(), path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            org.springframework.web.server.ResponseStatusException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        org.springframework.http.HttpStatusCode statusCode = ex.getStatusCode();
        ErrorCode errorCode = ErrorCodeMapper.mapHttpStatusToErrorCode(statusCode);
        
        ErrorResponse errorResponse = new ErrorResponse(errorCode.getCode(), ex.getReason())
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("ResponseStatusException: status={}, reason={} at path: {}", statusCode, ex.getReason(), path);
        
        return new ResponseEntity<>(errorResponse, statusCode);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        // Do not expose raw internal exception message to the client (Security constraint)
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.RUNTIME_ERROR.getCode(), "An internal server error occurred")
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.error("Unhandled RuntimeException at path: {}", path, ex);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        String path = getCleanPath(request);
        // Do not expose internal technical details in client response (Information Disclosure prevention)
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INTERNAL_ERROR.getCode(), "An unexpected error occurred")
                .withDescription("An unexpected error occurred on the server.")
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.error("Unhandled Exception at path: {}", path, ex);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.FORBIDDEN.getCode(), "Access denied")
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("AccessDeniedException: Access denied at path: {}", path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            org.springframework.security.authentication.BadCredentialsException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.BAD_CREDENTIALS.getCode(), "Invalid credentials")
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("BadCredentialsException: Invalid credentials at path: {}", path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            org.springframework.security.core.AuthenticationException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.AUTHENTICATION_FAILED.getCode(), "Authentication failed")
                .withDescription(ex.getMessage())
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("AuthenticationException: {} at path: {}", ex.getMessage(), path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            org.springframework.web.servlet.NoHandlerFoundException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.ENDPOINT_NOT_FOUND.getCode(), "Endpoint not found")
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("NoHandlerFoundException: Endpoint not found at path: {}", path);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            org.springframework.http.converter.HttpMessageNotReadableException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INVALID_REQUEST_BODY.getCode(), "Invalid request body")
                .withDescription("The HTTP request body is invalid or not readable.")
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("HttpMessageNotReadableException at path: {}: {}", path, ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            org.springframework.web.HttpMediaTypeNotSupportedException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode(), "Unsupported media type")
                .withDescription("Supported media types: " + ex.getSupportedMediaTypes())
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("HttpMediaTypeNotSupportedException at path: {}: {}", path, ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            org.springframework.web.HttpRequestMethodNotSupportedException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.METHOD_NOT_ALLOWED.getCode(), "Method not allowed")
                .withDescription("Supported methods: " + ex.getSupportedHttpMethods())
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("HttpRequestMethodNotSupportedException at path: {}: {}", path, ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(java.util.concurrent.TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeoutException(
            java.util.concurrent.TimeoutException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.TIMEOUT.getCode(), "Request timed out")
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("TimeoutException at path: {}: {}", path, ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            org.springframework.dao.DataIntegrityViolationException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        // Do not expose database integrity violation details to client
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.DATA_INTEGRITY_VIOLATION.getCode(), "Data integrity violation")
                .withDescription("Database integrity constraint violation occurred.")
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.error("DataIntegrityViolationException at path: {}", path, ex);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(
            org.springframework.orm.ObjectOptimisticLockingFailureException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.OPTIMISTIC_LOCK.getCode(), "Optimistic lock failure")
                .withDescription("The record was modified by another transaction")
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("ObjectOptimisticLockingFailureException at path: {}: {}", path, ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, WebRequest request) {
        
        String path = getCleanPath(request);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.PAYLOAD_TOO_LARGE.getCode(), "File size too large")
                .withDescription("The uploaded file exceeds the maximum allowed size. Please choose a smaller file.")
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
        
        addContextToErrorResponse(errorResponse, request);
        log.warn("MaxUploadSizeExceededException at path: {}: {}", path, ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }



    protected ErrorResponse createErrorResponse(String code, String message, String path) {
        return new ErrorResponse(code, message)
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
    }

    protected ErrorResponse createErrorResponse(String code, String message, String description, String path) {
        return new ErrorResponse(code, message, description)
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
    }

    protected ErrorResponse createValidationErrorResponse(List<String> errors, String path) {
        return ErrorResponse.fromValidation(errors)
                .withPath(path)
                .withTimestamp(java.time.LocalDateTime.now());
    }

    protected void addContextToErrorResponse(ErrorResponse errorResponse, WebRequest request) {
        if (errorResponse == null || request == null) {
            return;
        }
        String correlationId = request.getHeader("X-Correlation-ID");
        String requestId = request.getHeader("X-Request-ID");
        String userId = request.getHeader("X-User-ID");
        String tenantId = request.getHeader("X-Tenant-ID");

        if (correlationId != null) {
            errorResponse.withCorrelationId(correlationId);
        }
        if (requestId != null) {
            errorResponse.withRequestId(requestId);
        }
        if (userId != null) {
            errorResponse.withUserId(userId);
        }
        if (tenantId != null) {
            errorResponse.withTenantId(tenantId);
        }
    }
}
