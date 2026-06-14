package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.PaymentAuditLog;
import com.chatbot.core.simplepayment.model.PaymentAuditLog.AuditAction;
import com.chatbot.core.simplepayment.repository.PaymentAuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentAuditService {

    private final PaymentAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "sharedTransactionManager")
    public void logPaymentAction(
            String paymentReferenceCode,
            Long userId,
            Long tenantId,
            AuditAction action,
            String oldStatus,
            String newStatus,
            BigDecimal amount,
            String description,
            HttpServletRequest request) {

        try {
            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .paymentReferenceCode(paymentReferenceCode)
                    .userId(userId)
                    .tenantId(tenantId)
                    .action(action)
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .amount(amount)
                    .description(description)
                    .ipAddress(getClientIp(request))
                    .userAgent(getUserAgent(request))
                    .requestId(generateRequestId())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log saved for action: {}, payment: {}", action, paymentReferenceCode);

        } catch (Exception e) {
            log.error("Failed to save audit log for payment: {}, action: {}", paymentReferenceCode, action, e);
            // Don't throw exception to avoid affecting main transaction
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "sharedTransactionManager")
    public void logPaymentActionWithMetadata(
            String paymentReferenceCode,
            Long userId,
            Long tenantId,
            AuditAction action,
            String description,
            Map<String, Object> metadata) {

        try {
            String metadataJson = null;
            if (metadata != null && !metadata.isEmpty()) {
                try {
                    metadataJson = objectMapper.writeValueAsString(metadata);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to serialize metadata to JSON", e);
                }
            }

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .paymentReferenceCode(paymentReferenceCode)
                    .userId(userId)
                    .tenantId(tenantId)
                    .action(action)
                    .description(description)
                    .metadata(metadataJson)
                    .requestId(generateRequestId())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log saved with metadata for action: {}, payment: {}", action, paymentReferenceCode);

        } catch (Exception e) {
            log.error("Failed to save audit log with metadata for payment: {}, action: {}", paymentReferenceCode, action, e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String getUserAgent(HttpServletRequest request) {
        if (request == null) return null;
        return request.getHeader("User-Agent");
    }

    private String generateRequestId() {
        return java.util.UUID.randomUUID().toString();
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "sharedTransactionManager")
    public void logConfigChange(String configKey, String oldValue, String newValue, String updatedBy) {
        try {
            String description = String.format("Config changed: %s", configKey);
            if (oldValue != null && newValue != null) {
                description += String.format(" from '%s' to '%s'", 
                    isSensitive(configKey) ? "***" : oldValue, 
                    isSensitive(configKey) ? "***" : newValue);
            } else if (oldValue != null) {
                description += String.format(" deleted (was '%s')", isSensitive(configKey) ? "***" : oldValue);
            } else if (newValue != null) {
                description += String.format(" set to '%s'", isSensitive(configKey) ? "***" : newValue);
            }

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .paymentReferenceCode("CONFIG")
                    .userId(null)
                    .tenantId(null)
                    .action(AuditAction.CONFIG_CHANGED)
                    .description(description)
                    .requestId(generateRequestId())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Config change audit log saved for: {}", configKey);

        } catch (Exception e) {
            log.error("Failed to save config change audit log for: {}", configKey, e);
        }
    }

    private boolean isSensitive(String configKey) {
        return configKey != null && (
            configKey.contains("api-key") || 
            configKey.contains("secret") || 
            configKey.contains("password") ||
            configKey.contains("token")
        );
    }
}
