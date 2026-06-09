package com.chatbot.core.simplepayment.controller;

import com.chatbot.core.simplepayment.model.Invoice;
import com.chatbot.core.simplepayment.service.InvoiceService;
import com.chatbot.shared.constants.ApiConstants;
import com.chatbot.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoice Management", description = "API for managing payment invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * Generate invoice for payment
     */
    @PostMapping("/generate/{referenceCode}")
    @Operation(summary = "Generate invoice", description = "Generate invoice for a completed payment")
    public ResponseEntity<ApiResponse<Invoice>> generateInvoice(@PathVariable String referenceCode) {
        log.info("📄 Generating invoice for payment: {}", referenceCode);
        try {
            Invoice invoice = invoiceService.generateInvoice(referenceCode);
            return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice generated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get invoice by number
     */
    @GetMapping("/number/{invoiceNumber}")
    @Operation(summary = "Get invoice by number", description = "Get invoice by invoice number")
    public ResponseEntity<ApiResponse<Invoice>> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        log.info("🔍 Fetching invoice: {}", invoiceNumber);
        Invoice invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice retrieved successfully"));
    }

    /**
     * Get user invoices
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or #userId == authentication.principal.id")
    @Operation(summary = "Get user invoices", description = "Get all invoices for a user")
    public ResponseEntity<ApiResponse<List<Invoice>>> getUserInvoices(@PathVariable Long userId) {
        log.info("📋 Fetching invoices for user: {}", userId);
        List<Invoice> invoices = invoiceService.getUserInvoices(userId);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Invoices retrieved successfully"));
    }

    /**
     * Get tenant invoices
     */
    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get tenant invoices", description = "Get all invoices for a tenant (Admin only)")
    public ResponseEntity<ApiResponse<List<Invoice>>> getTenantInvoices(@PathVariable Long tenantId) {
        log.info("📋 Fetching invoices for tenant: {}", tenantId);
        List<Invoice> invoices = invoiceService.getTenantInvoices(tenantId);
        return ResponseEntity.ok(ApiResponse.success(invoices, "Invoices retrieved successfully"));
    }

    /**
     * Update invoice status (admin only)
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Update invoice status", description = "Update invoice status (Admin only)")
    public ResponseEntity<ApiResponse<Invoice>> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestParam Invoice.InvoiceStatus status) {
        log.info("🔄 Updating invoice status: {} to {}", id, status);
        try {
            Invoice invoice = invoiceService.updateInvoiceStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get invoice statistics (admin only)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get invoice statistics", description = "Get invoice statistics (Admin only)")
    public ResponseEntity<ApiResponse<String>> getInvoiceStatistics() {
        log.info("📊 Fetching invoice statistics");
        String stats = invoiceService.getInvoiceStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved successfully"));
    }
}
