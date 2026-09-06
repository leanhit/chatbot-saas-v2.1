package com.chatbot.core.payment.invoice.controller;

import com.chatbot.core.payment.invoice.model.Invoice;
import com.chatbot.core.payment.invoice.model.Invoice.InvoiceStatus;
import com.chatbot.core.payment.invoice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoice Management", description = "Invoice management endpoints")
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * Get invoice by invoice number
     */
    @GetMapping("/{invoiceNumber}")
    @Operation(
        summary = "Get invoice by number",
        description = "Get a specific invoice by its invoice number"
    )
    public ResponseEntity<Invoice> getInvoice(@PathVariable String invoiceNumber) {
        log.info("📄 Fetching invoice: {}", invoiceNumber);

        try {
            Invoice invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
            return ResponseEntity.ok(invoice);
        } catch (Exception e) {
            log.error("❌ Failed to fetch invoice: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get invoices for current user
     */
    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get user invoices",
        description = "Get all invoices for a specific user"
    )
    public ResponseEntity<List<Invoice>> getUserInvoices(@PathVariable Long userId) {
        log.info("📄 Fetching invoices for user: {}", userId);

        List<Invoice> invoices = invoiceService.getUserInvoices(userId);
        return ResponseEntity.ok(invoices);
    }

    /**
     * Get invoices for tenant
     */
    @GetMapping("/tenant/{tenantId}")
    @Operation(
        summary = "Get tenant invoices",
        description = "Get all invoices for a specific tenant"
    )
    public ResponseEntity<List<Invoice>> getTenantInvoices(@PathVariable Long tenantId) {
        log.info("📄 Fetching invoices for tenant: {}", tenantId);

        List<Invoice> invoices = invoiceService.getTenantInvoices(tenantId);
        return ResponseEntity.ok(invoices);
    }

    /**
     * Update invoice status - Admin only
     */
    @PutMapping("/{invoiceNumber}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update invoice status (Admin)",
        description = "Update invoice status - Admin only"
    )
    public ResponseEntity<Invoice> updateInvoiceStatus(
            @PathVariable String invoiceNumber,
            @RequestBody Map<String, String> request) {

        log.info("📄 Updating invoice status: {}", invoiceNumber);

        try {
            InvoiceStatus status = InvoiceStatus.valueOf(request.get("status"));
            Invoice invoice = invoiceService.updateInvoiceStatus(invoiceNumber, status);
            return ResponseEntity.ok(invoice);
        } catch (Exception e) {
            log.error("❌ Failed to update invoice status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete invoice - Admin only
     */
    @DeleteMapping("/{invoiceNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete invoice (Admin)",
        description = "Delete an invoice - Admin only"
    )
    public ResponseEntity<Map<String, String>> deleteInvoice(@PathVariable String invoiceNumber) {
        log.info("🗑️ Deleting invoice: {}", invoiceNumber);

        try {
            invoiceService.deleteInvoice(invoiceNumber);
            return ResponseEntity.ok(Map.of("message", "Invoice deleted successfully"));
        } catch (Exception e) {
            log.error("❌ Failed to delete invoice: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
