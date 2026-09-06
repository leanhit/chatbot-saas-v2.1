package com.chatbot.core.payment.merchant.controller;

import com.chatbot.core.payment.merchant.dto.MerchantPaymentRequest;
import com.chatbot.core.payment.merchant.dto.MerchantPaymentResponse;
import com.chatbot.core.payment.merchant.model.MerchantPaymentSession;
import com.chatbot.core.payment.merchant.service.MerchantPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Merchant Payment", description = "Merchant payment gateway APIs")
public class MerchantPaymentController {

    private final MerchantPaymentService merchantPaymentService;

    /**
     * Create a new payment session
     */
    @PostMapping("/sessions")
    @Operation(
        summary = "Create payment session",
        description = "Create a new payment session for merchant checkout"
    )
    public ResponseEntity<MerchantPaymentResponse> createPaymentSession(
            @RequestBody MerchantPaymentRequest request,
            HttpServletRequest httpRequest) {

        log.info("🏪 Creating merchant payment session");

        try {
            // Get merchant ID from filter
            Long merchantId = (Long) httpRequest.getAttribute("merchantId");
            if (merchantId == null) {
                return ResponseEntity.badRequest().build();
            }

            MerchantPaymentResponse response = merchantPaymentService.createPaymentSession(request, merchantId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to create payment session: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get payment session status
     */
    @GetMapping("/sessions/{sessionId}")
    @Operation(
        summary = "Get session status",
        description = "Get the status of a payment session"
    )
    public ResponseEntity<MerchantPaymentSession> getSessionStatus(@PathVariable String sessionId) {
        log.info("🏪 Getting session status: {}", sessionId);

        try {
            MerchantPaymentSession session = merchantPaymentService.getSessionStatus(sessionId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("❌ Failed to get session status: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cancel payment session
     */
    @PostMapping("/sessions/{sessionId}/cancel")
    @Operation(
        summary = "Cancel session",
        description = "Cancel a pending payment session"
    )
    public ResponseEntity<MerchantPaymentSession> cancelSession(@PathVariable String sessionId) {
        log.info("🏪 Cancelling session: {}", sessionId);

        try {
            MerchantPaymentSession session = merchantPaymentService.cancelSession(sessionId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("❌ Failed to cancel session: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all sessions for merchant
     */
    @GetMapping("/sessions")
    @Operation(
        summary = "Get merchant sessions",
        description = "Get all payment sessions for the authenticated merchant"
    )
    public ResponseEntity<List<MerchantPaymentSession>> getMerchantSessions(HttpServletRequest httpRequest) {
        log.info("🏪 Getting merchant sessions");

        try {
            Long merchantId = (Long) httpRequest.getAttribute("merchantId");
            if (merchantId == null) {
                return ResponseEntity.badRequest().build();
            }

            List<MerchantPaymentSession> sessions = merchantPaymentService.getMerchantSessions(merchantId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("❌ Failed to get merchant sessions: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Health check for merchant API
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Check if merchant payment API is healthy"
    )
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "merchant-payment-gateway",
            "timestamp", java.time.LocalDateTime.now()
        ));
    }
}
