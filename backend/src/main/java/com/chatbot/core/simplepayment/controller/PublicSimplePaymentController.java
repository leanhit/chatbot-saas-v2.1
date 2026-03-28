package com.chatbot.core.simplepayment.controller;

import com.chatbot.core.simplepayment.dto.DepositRequest;
import com.chatbot.core.simplepayment.dto.DepositResponse;
import com.chatbot.core.simplepayment.dto.PaymentStatusResponse;
import com.chatbot.core.simplepayment.service.BankApiService;
import com.chatbot.core.simplepayment.service.QRCodeService;
import com.chatbot.core.simplepayment.service.SimplePaymentService;
import com.chatbot.shared.utils.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/simple-payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Simple Payment", description = "Public simple bank transfer payment system (no auth)")
public class PublicSimplePaymentController {

    private final SimplePaymentService simplePaymentService;
    private final QRCodeService qrCodeService;
    private final BankApiService bankApiService;

    /**
     * Public health check - no authentication required
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check (public)",
        description = "Check if simple payment service is healthy - no authentication required"
    )
    public ResponseEntity<Map<String, Object>> healthCheck() {
        
        Map<String, Object> health = Map.of(
            "status", "healthy",
            "service", "simple-payment",
            "timestamp", java.time.LocalDateTime.now()
        );

        return ResponseEntity.ok(health);
    }

    /**
     * Public bank info - no authentication required
     */
    @GetMapping("/bank-info")
    @Operation(
        summary = "Get bank information (public)",
        description = "Get bank account information for manual transfer - no authentication required"
    )
    public ResponseEntity<QRCodeService.BankInfo> getBankInfo() {
        
        log.info("🏦 Getting bank information (public)");

        try {
            QRCodeService.BankInfo bankInfo = qrCodeService.getBankInfo();
            return ResponseEntity.ok(bankInfo);

        } catch (Exception e) {
            log.error("❌ Failed to get bank info: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Public create deposit request - no authentication required
     */
    @PostMapping("/deposit")
    @Operation(
        summary = "Create deposit request (public)",
        description = "Create a new deposit request with QR code for bank transfer - no authentication required"
    )
    public ResponseEntity<DepositResponse> createDeposit(@RequestBody DepositRequest request) {
        
        log.info("📱 Creating public deposit request: {} VND", request.getAmount());

        try {
            // Mock user and tenant IDs for public testing
            Long userId = 1L;
            Long tenantId = 1L;

            DepositResponse response = simplePaymentService.createDeposit(request, userId, tenantId);
            // Apply DateUtils formatting
            response.withFormattedDates();
            
            log.info("✅ Public deposit request created: {}", response.getReferenceCode());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to create public deposit request: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Public check payment status
     */
    @GetMapping("/status/{referenceCode}")
    @Operation(
        summary = "Check payment status (public)",
        description = "Check the status of a payment by reference code - no authentication required"
    )
    public ResponseEntity<PaymentStatusResponse> checkPaymentStatus(@PathVariable String referenceCode) {
        
        log.info("🔍 Checking public payment status: {}", referenceCode);

        try {
            PaymentStatusResponse response = simplePaymentService.checkPaymentStatus(referenceCode);
            // Apply DateUtils formatting
            response.withFormattedDates();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to check public payment status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Public simulate payment
     */
    @PostMapping("/test/simulate-payment")
    @Operation(
        summary = "Simulate bank payment (public test)",
        description = "Simulate a bank transaction for testing purposes - no authentication required"
    )
    public ResponseEntity<String> simulatePayment(@RequestBody Map<String, Object> request) {
        
        log.info("🧪 Simulating public bank payment");

        try {
            String referenceCode = (String) request.get("referenceCode");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());

            bankApiService.simulateBankTransaction(referenceCode, amount);

            return ResponseEntity.ok("Payment simulated successfully");

        } catch (Exception e) {
            log.error("❌ Failed to simulate public payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Simulation failed: " + e.getMessage());
        }
    }

    /**
     * Public payment history
     */
    @GetMapping("/history")
    @Operation(
        summary = "Get payment history (public)",
        description = "Get all payments for mock user - no authentication required"
    )
    public ResponseEntity<List<PaymentStatusResponse>> getPaymentHistory() {

        try {
            // Mock user and tenant IDs for public testing
            Long userId = 1L;
            Long tenantId = 1L;

            List<PaymentStatusResponse> payments = simplePaymentService.getUserPayments(userId, tenantId);
            // Apply DateUtils formatting to all payments
            payments.forEach(PaymentStatusResponse::withFormattedDates);
            return ResponseEntity.ok(payments);

        } catch (Exception e) {
            log.error("❌ Failed to get public payment history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
