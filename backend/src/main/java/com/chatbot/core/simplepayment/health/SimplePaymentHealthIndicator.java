package com.chatbot.core.simplepayment.health;

import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import com.chatbot.core.simplepayment.service.BankApiService;
import com.chatbot.core.simplepayment.service.QRCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SimplePaymentHealthIndicator implements HealthIndicator {

    private final SimplePaymentRepository paymentRepository;
    private final BankApiService bankApiService;
    private final QRCodeService qrCodeService;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            // Check database connectivity
            long totalPayments = paymentRepository.count();
            details.put("database", "UP");
            details.put("totalPayments", totalPayments);
            
            // Check pending payments count
            long pendingPayments = paymentRepository.findActivePendingPayments(LocalDateTime.now()).size();
            details.put("pendingPayments", pendingPayments);
            
            // Check bank API connectivity
            boolean bankApiHealthy = checkBankApiHealth();
            details.put("bankApi", bankApiHealthy ? "UP" : "DOWN");
            
            // Check QR code service
            boolean qrServiceHealthy = checkQRServiceHealth();
            details.put("qrService", qrServiceHealthy ? "UP" : "DOWN");
            
            // Overall status
            boolean isHealthy = bankApiHealthy && qrServiceHealthy;
            
            if (isHealthy) {
                return Health.up()
                    .withDetails(details)
                    .build();
            } else {
                return Health.down()
                    .withDetails(details)
                    .build();
            }
            
        } catch (Exception e) {
            log.error("SimplePayment health check failed", e);
            details.put("error", e.getMessage());
            return Health.down()
                .withDetails(details)
                .build();
        }
    }
    
    private boolean checkBankApiHealth() {
        try {
            // Simple health check - in production, this should call a real health endpoint
            return bankApiService != null;
        } catch (Exception e) {
            log.warn("Bank API health check failed", e);
            return false;
        }
    }
    
    private boolean checkQRServiceHealth() {
        try {
            // Simple health check
            return qrCodeService != null;
        } catch (Exception e) {
            log.warn("QR service health check failed", e);
            return false;
        }
    }
}
