package com.chatbot.core.billing.payment.controller;

import com.chatbot.core.billing.payment.dto.PaymentMethodRequest;
import com.chatbot.core.billing.payment.dto.PaymentMethodResponse;
import com.chatbot.core.billing.payment.service.PaymentMethodService;
import com.chatbot.core.tenant.service.TenantService;
import com.chatbot.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/billing/payment-methods")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Method Management", description = "APIs for managing payment methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;
    private final TenantService tenantService;

    @Operation(
        summary = "Get payment methods for tenant", 
        description = "Retrieve all payment methods for a specific tenant",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment methods retrieved successfully",
                content = @Content(schema = @Schema(implementation = List.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
        }
    )
    @GetMapping("/{tenantKey}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getPaymentMethods(
            @Parameter(description = "Tenant key") @PathVariable String tenantKey) {
        
        // Convert tenant key to tenant ID
        Long tenantId = tenantService.getTenantIdByKey(tenantKey);
        if (tenantId == null) {
            throw new RuntimeException("Tenant not found with key: " + tenantKey);
        }
        
        List<PaymentMethodResponse> paymentMethods = paymentMethodService.getPaymentMethodsByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(paymentMethods));
    }

    @Operation(
        summary = "Create payment method", 
        description = "Create a new payment method for a tenant",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment method created successfully",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payment method data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> createPaymentMethod(
            @RequestParam Long tenantId,
            @Valid @RequestBody PaymentMethodRequest request) {
        
        PaymentMethodResponse response = paymentMethodService.createPaymentMethod(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment method created successfully"));
    }

    @Operation(
        summary = "Update payment method", 
        description = "Update an existing payment method",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment method updated successfully",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payment method data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment method not found")
        }
    )
    @PutMapping("/{methodId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> updatePaymentMethod(
            @Parameter(description = "Payment method ID") @PathVariable Long methodId,
            @Valid @RequestBody PaymentMethodRequest request) {
        
        PaymentMethodResponse response = paymentMethodService.updatePaymentMethod(methodId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment method updated successfully"));
    }

    @Operation(
        summary = "Delete payment method", 
        description = "Remove a payment method",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment method deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment method not found")
        }
    )
    @DeleteMapping("/{methodId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePaymentMethod(
            @Parameter(description = "Payment method ID") @PathVariable Long methodId) {
        
        paymentMethodService.deletePaymentMethod(methodId);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment method deleted successfully"));
    }

    @Operation(
        summary = "Set default payment method", 
        description = "Set a payment method as the default for a tenant",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Default payment method set successfully",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment method or tenant not found")
        }
    )
    @PostMapping("/{tenantKey}/default/{methodId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> setDefaultPaymentMethod(
            @Parameter(description = "Tenant key") @PathVariable String tenantKey,
            @Parameter(description = "Payment method ID") @PathVariable Long methodId) {
        
        // Convert tenant key to tenant ID
        Long tenantId = tenantService.getTenantIdByKey(tenantKey);
        if (tenantId == null) {
            throw new RuntimeException("Tenant not found with key: " + tenantKey);
        }
        
        PaymentMethodResponse response = paymentMethodService.setDefaultPaymentMethod(tenantId, methodId);
        return ResponseEntity.ok(ApiResponse.success(response, "Default payment method set successfully"));
    }
}
