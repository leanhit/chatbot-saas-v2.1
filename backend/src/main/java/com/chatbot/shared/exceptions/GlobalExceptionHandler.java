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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromNotFound(ex.getMessage())
                .withCode("NOT_FOUND")
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromValidation(ex.getErrors())
                .withCode("VALIDATION_ERROR")
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromUnauthorized(ex.getMessage())
                .withCode("UNAUTHORIZED")
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.fromValidation(errors)
                .withCode("VALIDATION_ERROR")
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex, WebRequest request) {
        
        List<String> errors = ex.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.fromValidation(errors)
                .withCode("VALIDATION_ERROR")
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.fromValidation(errors)
                .withCode("VALIDATION_ERROR")
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromCode("BAD_REQUEST", ex.getMessage())
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromCode("CONFLICT", ex.getMessage())
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromInternalError(ex.getMessage())
                .withCode("RUNTIME_ERROR")
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromInternalError("An unexpected error occurred")
                .withCode("INTERNAL_ERROR")
                .withDescription(ex.getMessage())
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromForbidden("Access denied")
                .withCode("FORBIDDEN")
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            org.springframework.security.authentication.BadCredentialsException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromUnauthorized("Invalid credentials")
                .withCode("BAD_CREDENTIALS")
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            org.springframework.security.core.AuthenticationException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromUnauthorized("Authentication failed")
                .withCode("AUTHENTICATION_FAILED")
                .withDescription(ex.getMessage())
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            org.springframework.web.servlet.NoHandlerFoundException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromNotFound("Endpoint not found")
                .withCode("ENDPOINT_NOT_FOUND")
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            org.springframework.http.converter.HttpMessageNotReadableException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromCode("INVALID_REQUEST_BODY", "Invalid request body")
                .withDescription(ex.getMessage())
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
            org.springframework.web.HttpMediaTypeNotSupportedException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromCode("UNSUPPORTED_MEDIA_TYPE", "Unsupported media type")
                .withDescription("Supported media types: " + ex.getSupportedMediaTypes())
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            org.springframework.web.HttpRequestMethodNotSupportedException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromCode("METHOD_NOT_ALLOWED", "Method not allowed")
                .withDescription("Supported methods: " + ex.getSupportedHttpMethods())
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(java.util.concurrent.TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeoutException(
            java.util.concurrent.TimeoutException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromTimeout("Request timed out")
                .withDescription(ex.getMessage())
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            org.springframework.dao.DataIntegrityViolationException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromCode("DATA_INTEGRITY_VIOLATION", "Data integrity violation")
                .withDescription(ex.getMessage())
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(
            org.springframework.orm.ObjectOptimisticLockingFailureException ex, WebRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.fromCode("OPTIMISTIC_LOCK", "Optimistic lock failure")
                .withDescription("The record was modified by another transaction")
                .withPath(request.getDescription(false))
                .withTimestamp(java.time.LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
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
