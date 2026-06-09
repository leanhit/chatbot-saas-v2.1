package com.chatbot.core.simplepayment.aspect;

import com.chatbot.core.simplepayment.model.PaymentAuditLog.AuditAction;
import com.chatbot.core.simplepayment.service.PaymentAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentAuditAspect {

    private final PaymentAuditService paymentAuditService;

    @Around("@annotation(com.chatbot.core.simplepayment.annotation.AuditPayment)")
    public Object auditPaymentOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        log.debug("Auditing payment operation: {}", methodName);
        
        try {
            Object result = joinPoint.proceed();
            
            // Extract parameters for audit logging
            String referenceCode = extractReferenceCode(args);
            Long userId = extractUserId(args);
            Long tenantId = extractTenantId(args);
            AuditAction action = determineAction(methodName);
            
            if (referenceCode != null && userId != null && tenantId != null) {
                paymentAuditService.logPaymentAction(
                    referenceCode,
                    userId,
                    tenantId,
                    action,
                    null,
                    null,
                    null,
                    "Operation completed: " + methodName,
                    null
                );
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Payment operation failed: {}", methodName, e);
            throw e;
        }
    }

    private String extractReferenceCode(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof String) {
                String str = (String) arg;
                if (str.startsWith("PAY")) {
                    return str;
                }
            }
        }
        return null;
    }

    private Long extractUserId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        return null;
    }

    private Long extractTenantId(Object[] args) {
        // Simple extraction - in real implementation, use proper parameter mapping
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Long && i > 0) {
                return (Long) args[i];
            }
        }
        return null;
    }

    private AuditAction determineAction(String methodName) {
        return switch (methodName) {
            case "createDeposit" -> AuditAction.PAYMENT_CREATED;
            case "completePayment" -> AuditAction.PAYMENT_COMPLETED;
            case "cancelPayment" -> AuditAction.PAYMENT_CANCELLED;
            case "refundPayment" -> AuditAction.PAYMENT_REFUNDED;
            case "retryPayment" -> AuditAction.PAYMENT_RETRIED;
            default -> AuditAction.PAYMENT_CREATED;
        };
    }
}
