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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simple-payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Simple Payment", description = "Simple bank transfer payment system")
public class SimplePaymentController {

    private final SimplePaymentService simplePaymentService;
    private final QRCodeService qrCodeService;
    private final BankApiService bankApiService;

    /**
     * Tạo yêu cầu nạp tiền mới
     */
    @PostMapping("/deposit")
    @Operation(
        summary = "Create deposit request",
        description = "Create a new deposit request with QR code for bank transfer"
    )
    public ResponseEntity<DepositResponse> createDeposit(
            @RequestBody DepositRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        
        log.info("📱 Creating deposit request for user: {}", userDetails.getUsername());

        try {
            // Extract user ID and tenant ID from user details (simplified)
            Long userId = extractUserId(userDetails);
            Long tenantId = extractTenantId(httpRequest);

            DepositResponse response = simplePaymentService.createDeposit(request, userId, tenantId);
            
            log.info("✅ Deposit request created: {}", response.getReferenceCode());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to create deposit request: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/status/{referenceCode}")
    @Operation(
        summary = "Check payment status",
        description = "Check the status of a payment by reference code"
    )
    public ResponseEntity<PaymentStatusResponse> checkPaymentStatus(@PathVariable String referenceCode) {
        
        log.info("🔍 Checking payment status: {}", referenceCode);

        try {
            PaymentStatusResponse response = simplePaymentService.checkPaymentStatus(referenceCode);
            // Apply DateUtils formatting
            response.withFormattedDates();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to check payment status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lấy danh sách thanh toán của user
     */
    @GetMapping("/history")
    @Operation(
        summary = "Get payment history",
        description = "Get all payments for the current user"
    )
    public ResponseEntity<List<PaymentStatusResponse>> getPaymentHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        try {
            Long userId = extractUserId(userDetails);
            Long tenantId = extractTenantId(httpRequest);

            List<PaymentStatusResponse> payments = simplePaymentService.getUserPayments(userId, tenantId);
            // Apply DateUtils formatting to all payments
            payments.forEach(PaymentStatusResponse::withFormattedDates);
            return ResponseEntity.ok(payments);

        } catch (Exception e) {
            log.error("❌ Failed to get payment history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lấy thông tin ngân hàng
     */
    @GetMapping("/bank-info")
    @Operation(
        summary = "Get bank information",
        description = "Get bank account information for manual transfer"
    )
    public ResponseEntity<QRCodeService.BankInfo> getBankInfo() {
        
        log.info("🏦 Getting bank information");

        try {
            QRCodeService.BankInfo bankInfo = qrCodeService.getBankInfo();
            return ResponseEntity.ok(bankInfo);

        } catch (Exception e) {
            log.error("❌ Failed to get bank info: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Test endpoint để simulate bank transaction
     */
    @PostMapping("/test/simulate-payment")
    @Operation(
        summary = "Simulate bank payment (test only)",
        description = "Simulate a bank transaction for testing purposes"
    )
    public ResponseEntity<String> simulatePayment(@RequestBody Map<String, Object> request) {
        
        log.info("🧪 Simulating bank payment");

        try {
            String referenceCode = (String) request.get("referenceCode");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());

            bankApiService.simulateBankTransaction(referenceCode, amount);

            return ResponseEntity.ok("Payment simulated successfully");

        } catch (Exception e) {
            log.error("❌ Failed to simulate payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Simulation failed: " + e.getMessage());
        }
    }

    /**
     * Manual complete payment (admin only)
     */
    @PostMapping("/admin/complete/{referenceCode}")
    @Operation(
        summary = "Complete payment manually (admin)",
        description = "Manually complete a payment (admin only)"
    )
    public ResponseEntity<String> manualCompletePayment(
            @PathVariable String referenceCode,
            @RequestBody Map<String, String> request) {

        log.info("🔧 Manually completing payment: {}", referenceCode);

        try {
            String bankTransactionId = request.get("bankTransactionId");
            simplePaymentService.completePayment(referenceCode, bankTransactionId);

            return ResponseEntity.ok("Payment completed successfully");

        } catch (Exception e) {
            log.error("❌ Failed to complete payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to complete payment: " + e.getMessage());
        }
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Check if simple payment service is healthy"
    )
    public ResponseEntity<Map<String, Object>> healthCheck() {
        
        Map<String, Object> health = Map.of(
            "status", "healthy",
            "service", "simple-payment",
            "timestamp", java.time.LocalDateTime.now()
        );

        return ResponseEntity.ok(health);
    }

    // Helper methods (simplified - in real implementation, get from user context)
    private Long extractUserId(UserDetails userDetails) {
        // In real implementation, extract from user context or database
        return 1L; // Mock user ID
    }

    private Long extractTenantId(HttpServletRequest request) {
        // In real implementation, extract from request context or user session
        return 1L; // Mock tenant ID
    }
}
