package com.chatbot.core.simplepayment.controller;

import com.chatbot.core.simplepayment.dto.DepositRequest;
import com.chatbot.core.simplepayment.dto.DepositResponse;
import com.chatbot.core.simplepayment.dto.PaymentStatusResponse;
import com.chatbot.core.simplepayment.service.BankApiService;
import com.chatbot.core.simplepayment.service.QRCodeService;
import com.chatbot.core.simplepayment.service.SimplePaymentService;
import com.chatbot.core.simplepayment.service.PaymentCancellationService;
import com.chatbot.core.simplepayment.service.PaymentRefundService;
import com.chatbot.core.simplepayment.service.PaymentRetryService;
import com.chatbot.core.simplepayment.service.PaymentContextService;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;

import com.chatbot.shared.utils.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

import java.util.HashMap;
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
    private final PaymentContextService paymentContextService;
    private final PaymentCancellationService paymentCancellationService;
    private final PaymentRefundService paymentRefundService;
    private final PaymentRetryService paymentRetryService;
    private final com.chatbot.core.simplepayment.validation.PaymentValidationService paymentValidationService;
    private final com.chatbot.core.simplepayment.service.SystemConfigService systemConfigService;
    private final TenantPermissionValidator tenantPermissionValidator;

    /**
     * Tạo yêu cầu nạp tiền mới (OWNER only)
     */
    @PostMapping("/deposit")
    @Operation(
        summary = "Create deposit request",
        description = "Create a new deposit request with QR code for bank transfer (OWNER only)"
    )
    public ResponseEntity<Object> createDeposit(
            @RequestBody DepositRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        log.info("📱 Creating deposit request for user: {}", userDetails.getUsername());

        try {
            // Validate deposit request
            paymentValidationService.validateDepositRequest(request);

            // Extract user ID and tenant ID from user details
            Long userId = paymentContextService.extractUserId(userDetails);
            Long tenantId = paymentContextService.extractTenantId(httpRequest);

            paymentContextService.validateTenantAccess(userId, tenantId);

            // Check if user is OWNER of the tenant
            String userEmail = userDetails.getUsername();
            if (!tenantPermissionValidator.isOwner(tenantId, userEmail)) {
                throw new InsufficientPermissionException("Only OWNER can create deposits");
            }

            DepositResponse response = simplePaymentService.createDeposit(request, userId, tenantId);

            log.info("✅ Deposit request created: {}", response.getReferenceCode());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to create deposit request: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
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
            // Validate reference code
            paymentValidationService.validateReferenceCode(referenceCode);

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
            Long userId = paymentContextService.extractUserId(userDetails);
            Long tenantId = paymentContextService.extractTenantId(httpRequest);

            paymentContextService.validateTenantAccess(userId, tenantId);

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
     * Cập nhật thông tin ngân hàng (admin only)
     */
    @PutMapping("/admin/bank-info")
    @PreAuthorize("hasAnyRole('ADMIN','SYSTEM_ADMIN')")
    @Operation(
        summary = "Update bank information (admin)",
        description = "Update bank account information and bank API configuration (Admin only)"
    )
    public ResponseEntity<String> updateBankInfo(
            @RequestBody QRCodeService.BankInfo bankInfo,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("🏦 Updating bank information by admin: {}", userDetails.getUsername());

        try {
            // Update in-memory config for immediate effect
            qrCodeService.updateBankInfo(bankInfo);
            
            // Persist to database for runtime configuration
            systemConfigService.saveBankConfig(bankInfo, userDetails.getUsername());
            
            return ResponseEntity.ok("Bank information updated successfully");

        } catch (Exception e) {
            log.error("❌ Failed to update bank info: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to update bank info: " + e.getMessage());
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
     * Public health check (no authentication required)
     */
    @GetMapping("/public/health")
    @Operation(
        summary = "Public health check",
        description = "Check if simple payment service is healthy (no auth required)"
    )
    public ResponseEntity<Map<String, Object>> publicHealthCheck() {
        
        Map<String, Object> health = Map.of(
            "status", "healthy",
            "service", "simple-payment",
            "timestamp", java.time.LocalDateTime.now(),
            "packages", "loaded"
        );

        return ResponseEntity.ok(health);
    }

    /**
     * Manual trigger payment checking (admin only)
     */
    @PostMapping("/admin/check-payments")
    @Operation(
        summary = "Check pending payments manually (admin)",
        description = "Manually trigger pending payment check (admin only)"
    )
    public ResponseEntity<String> checkPendingPaymentsManually() {
        log.info("🔧 Manually triggering payment check");
        
        try {
            simplePaymentService.checkPendingPayments();
            return ResponseEntity.ok("Payment check triggered successfully");
        } catch (Exception e) {
            log.error("❌ Failed to trigger payment check: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to trigger payment check: " + e.getMessage());
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

    /**
     * Debug endpoint to check current user and tenant context
     */
    @GetMapping("/debug/context")
    @Operation(
        summary = "Debug user and tenant context",
        description = "Check current authentication and tenant context for debugging"
    )
    public Map<String, Object> debugContext(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        
        Map<String, Object> debug = new HashMap<>();
        
        try {
            Long userId = paymentContextService.extractUserId(userDetails);
            Long tenantId = paymentContextService.extractTenantId(httpRequest);
            
            debug.put("authenticatedUser", userDetails.getUsername());
            debug.put("extractedUserId", userId);
            debug.put("requestedTenantId", tenantId);
            debug.put("hasTenant", tenantId != null);
            
        } catch (Exception e) {
            debug.put("error", e.getMessage());
        }
        
        return debug;
    }

    /**
     * Cancel payment (OWNER only)
     */
    @PostMapping("/cancel/{referenceCode}")
    @Operation(
        summary = "Cancel payment",
        description = "Cancel a pending payment before it expires (OWNER only)"
    )
    public ResponseEntity<String> cancelPayment(
            @PathVariable String referenceCode,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        log.info("🚫 User cancelling payment: {}", referenceCode);

        try {
            Long userId = paymentContextService.extractUserId(userDetails);
            Long tenantId = paymentContextService.extractTenantId(httpRequest);
            String reason = request.getOrDefault("reason", "User requested cancellation");

            // Check if user is OWNER of the tenant
            String userEmail = userDetails.getUsername();
            if (!tenantPermissionValidator.isOwner(tenantId, userEmail)) {
                throw new InsufficientPermissionException("Only OWNER can cancel payments");
            }

            com.chatbot.core.simplepayment.model.SimplePayment payment =
                simplePaymentService.getPaymentByReference(referenceCode);

            // Security check: user can only cancel their own payments
            if (!payment.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only cancel your own payments");
            }

            com.chatbot.core.simplepayment.model.SimplePayment cancelled =
                paymentCancellationService.cancelPayment(referenceCode, reason);

            return ResponseEntity.ok("Payment cancelled successfully");

        } catch (Exception e) {
            log.error("❌ Failed to cancel payment {}: {}", referenceCode, e.getMessage());
            return ResponseEntity.badRequest().body("Failed to cancel payment: " + e.getMessage());
        }
    }

    /**
     * Refund payment (admin only)
     */
    @PostMapping("/admin/refund/{referenceCode}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(
        summary = "Refund payment (admin)",
        description = "Refund a completed payment (Admin only)"
    )
    public ResponseEntity<String> refundPayment(
            @PathVariable String referenceCode,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("💰 Admin refunding payment: {}", referenceCode);
        
        try {
            Long adminUserId = paymentContextService.extractUserId(userDetails);
            String reason = request.getOrDefault("reason", "Admin refund");
            
            com.chatbot.core.simplepayment.model.SimplePayment refunded = 
                paymentRefundService.refundPayment(referenceCode, reason, adminUserId);
            
            return ResponseEntity.ok("Payment refunded successfully");
            
        } catch (Exception e) {
            log.error("❌ Failed to refund payment {}: {}", referenceCode, e.getMessage());
            return ResponseEntity.badRequest().body("Failed to refund payment: " + e.getMessage());
        }
    }

    /**
     * Retry failed payment (OWNER only)
     */
    @PostMapping("/retry/{referenceCode}")
    @Operation(
        summary = "Retry payment",
        description = "Retry a failed or expired payment (OWNER only)"
    )
    public ResponseEntity<Object> retryPayment(
            @PathVariable String referenceCode,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {

        log.info("🔄 Retrying payment: {}", referenceCode);

        try {
            Long userId = paymentContextService.extractUserId(userDetails);
            Long tenantId = paymentContextService.extractTenantId(httpRequest);

            // Check if user is OWNER of the tenant
            String userEmail = userDetails.getUsername();
            if (!tenantPermissionValidator.isOwner(tenantId, userEmail)) {
                throw new InsufficientPermissionException("Only OWNER can retry payments");
            }

            com.chatbot.core.simplepayment.dto.DepositResponse response =
                paymentRetryService.retryPayment(referenceCode, userId, tenantId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to retry payment {}: {}", referenceCode, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Check deposit limits for free packages
     */
    @GetMapping("/deposit-limits")
    @Operation(
        summary = "Check deposit limits",
        description = "Check current deposit limits for free packages"
    )
    public ResponseEntity<Map<String, Object>> checkDepositLimits(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        
        try {
            Long userId = paymentContextService.extractUserId(userDetails);
            Long tenantId = paymentContextService.extractTenantId(httpRequest);
            
            paymentContextService.validateTenantAccess(userId, tenantId);
            
            log.info("🔍 [DEPOSIT LIMITS] Checking limits for user: {}, tenant: {}", userId, tenantId);
            
            // Get current deposit limits from service
            Map<String, Object> limits = simplePaymentService.getCurrentDepositLimits(userId, tenantId);
            
            return ResponseEntity.ok(limits);
            
        } catch (Exception e) {
            log.error("❌ [DEPOSIT LIMITS] Error checking limits: {}", e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to check deposit limits");
            error.put("message", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(error);
        }
    }


}
