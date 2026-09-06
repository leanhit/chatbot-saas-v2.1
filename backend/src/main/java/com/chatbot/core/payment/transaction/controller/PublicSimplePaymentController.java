package com.chatbot.core.payment.transaction.controller;

import com.chatbot.core.payment.transaction.dto.DepositRequest;
import com.chatbot.core.payment.transaction.dto.DepositResponse;
import com.chatbot.core.payment.transaction.dto.PaymentStatusResponse;
import com.chatbot.core.payment.transaction.service.SimplePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Payment", description = "Public payment endpoints (no auth)")
public class PublicSimplePaymentController {

    private final SimplePaymentService simplePaymentService;

    /**
     * Public health check - no authentication required
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check (public)",
        description = "Check if payment service is healthy - no authentication required"
    )
    public ResponseEntity<Map<String, Object>> healthCheck() {
        
        Map<String, Object> health = Map.of(
            "status", "healthy",
            "service", "payment-transaction",
            "timestamp", java.time.LocalDateTime.now()
        );

        return ResponseEntity.ok(health);
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
            response.withFormattedDates();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to check public payment status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Public create deposit request - no authentication required
     */
    @PostMapping("/deposit")
    @Operation(
        summary = "Create deposit request (public)",
        description = "Create a new deposit request - no authentication required"
    )
    public ResponseEntity<DepositResponse> createDeposit(@RequestBody DepositRequest request) {
        
        log.info("📱 Creating public deposit request: {} VND", request.getAmount());

        try {
            // Get authenticated user ID from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Placeholder - will be implemented after user service integration
            log.info("📱 Public deposit request - user authentication required");
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("❌ Failed to create public deposit request: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Payment history for the authenticated user's current tenant.
     */
    @GetMapping("/history")
    @Operation(
        summary = "Get payment history",
        description = "Get payment history for the authenticated user"
    )
    public ResponseEntity<List<PaymentStatusResponse>> getPaymentHistory() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Placeholder - will be implemented after user service integration
            log.info("📱 Public payment history - user authentication required");
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("❌ Failed to get payment history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Simulate bank payment - ADMIN only, for testing purposes.
     */
    @PostMapping("/test/simulate-payment")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Simulate bank payment (ADMIN only)",
        description = "Simulate a bank transaction for testing purposes - requires ADMIN role"
    )
    public ResponseEntity<String> simulatePayment(@RequestBody Map<String, Object> request) {
        
        log.info("🧪 Simulating bank payment (ADMIN)");

        try {
            String referenceCode = (String) request.get("referenceCode");
            
            // Placeholder - will be implemented after gateway migration
            log.info("🧪 Simulation will be implemented after gateway migration");
            
            return ResponseEntity.ok("Payment simulation will be implemented after gateway migration");

        } catch (Exception e) {
            log.error("❌ Failed to simulate payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Simulation failed: " + e.getMessage());
        }
    }
}
