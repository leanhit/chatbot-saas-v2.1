package com.chatbot.core.license.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class LicenseExceptionHandler {

    @ExceptionHandler(LicenseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleLicenseNotFound(
            LicenseNotFoundException ex, WebRequest request) {
        return createErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(LicenseExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleLicenseExpired(
            LicenseExpiredException ex, WebRequest request) {
        return createErrorResponse(ex, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(LicenseException.class)
    public ResponseEntity<Map<String, Object>> handleLicense(
            LicenseException ex, WebRequest request) {
        // Handle specific license errors according to req.md
        if (ex.getMessage().contains("expired")) {
            return createErrorResponse(ex, HttpStatus.UNAUTHORIZED, request);
        } else if (ex.getMessage().contains("inactive")) {
            return createErrorResponse(ex, HttpStatus.UNAUTHORIZED, request);
        } else {
            return createErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
        }
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(
            LicenseException ex, HttpStatus status, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("errorCode", ex.getErrorCode());
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(status).body(response);
    }
}
