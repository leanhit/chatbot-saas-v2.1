package com.chatbot.core.payment.transaction.controller;

import com.chatbot.core.payment.transaction.dto.DepositRequest;
import com.chatbot.core.payment.transaction.dto.DepositResponse;
import com.chatbot.core.payment.transaction.dto.PaymentStatusResponse;
import com.chatbot.core.payment.transaction.service.PaymentSseService;
import com.chatbot.core.payment.transaction.service.SimplePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/public/payment", "/api/public/simple-payment"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Payment", description = "Public payment endpoints (no auth)")
public class PublicSimplePaymentController {

    private final SimplePaymentService simplePaymentService;
    private final PaymentSseService paymentSseService;

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
     * Subscribe to SSE payment status updates for reference code
     */
    @GetMapping(value = "/events/{referenceCode}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "SSE Payment Status Stream",
        description = "Subscribe to live payment status updates using Server-Sent Events (SSE)"
    )
    public SseEmitter subscribePaymentEvents(@PathVariable String referenceCode) {
        log.info("📶 Public SSE subscription for payment: {}", referenceCode);
        return paymentSseService.subscribe(referenceCode);
    }

    /**
     * Public create deposit request
     */
    @PostMapping("/deposit")
    @Operation(
        summary = "Create deposit request (public)",
        description = "Create a new deposit request"
    )
    public ResponseEntity<DepositResponse> createDeposit(@RequestBody DepositRequest request) {
        log.info("📱 Creating public deposit request: {} VND", request.getAmount());
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ Failed to create public deposit request: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Payment history for authenticated user
     */
    @GetMapping("/history")
    @Operation(
        summary = "Get payment history",
        description = "Get payment history for authenticated user"
    )
    public ResponseEntity<List<PaymentStatusResponse>> getPaymentHistory() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ Failed to get payment history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Simulate bank payment for testing purposes.
     */
    @PostMapping({"/test/simulate-payment", "/simulate-payment"})
    @Operation(
        summary = "Simulate bank payment",
        description = "Simulate bank transfer payment completion for testing purposes"
    )
    public ResponseEntity<Object> simulatePayment(@RequestBody Map<String, Object> request) {
        log.info("🧪 Simulating bank payment for request: {}", request);
        try {
            String referenceCode = (String) request.get("referenceCode");
            if (referenceCode == null || referenceCode.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "referenceCode is required");
                return ResponseEntity.badRequest().body(error);
            }

            String bankTxId = "SIM" + System.currentTimeMillis();
            simplePaymentService.completePayment(referenceCode, bankTxId);

            PaymentStatusResponse response = simplePaymentService.checkPaymentStatus(referenceCode);
            response.withFormattedDates();

            log.info("✅ Bank payment simulated successfully for: {}", referenceCode);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Payment simulated successfully",
                "referenceCode", referenceCode,
                "bankTransactionId", bankTxId,
                "payment", response
            ));

        } catch (Exception e) {
            log.error("❌ Failed to simulate payment: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Simulation failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
