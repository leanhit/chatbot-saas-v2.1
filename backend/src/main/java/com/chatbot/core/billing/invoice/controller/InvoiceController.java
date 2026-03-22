package com.chatbot.core.billing.invoice.controller;

import com.chatbot.core.billing.invoice.dto.InvoiceRequest;
import com.chatbot.core.billing.invoice.dto.InvoiceResponse;
import com.chatbot.core.billing.invoice.service.InvoiceService;
import com.chatbot.core.tenant.service.TenantService;
import com.chatbot.shared.dto.ApiResponse;
import com.chatbot.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/billing/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoice Management", description = "APIs for managing invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final TenantService tenantService;

    @Operation(
        summary = "Get invoices for tenant", 
        description = "Retrieve all invoices for a specific tenant with pagination",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoices retrieved successfully",
                content = @Content(schema = @Schema(implementation = List.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
        }
    )
    @GetMapping("/{tenantKey}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> getInvoices(
            @Parameter(description = "Tenant key") @PathVariable String tenantKey,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        // Convert tenant key to tenant ID
        Long tenantId = tenantService.getTenantIdByKey(tenantKey);
        if (tenantId == null) {
            throw new RuntimeException("Tenant not found with key: " + tenantKey);
        }
        
        List<InvoiceResponse> invoices = invoiceService.getInvoicesByTenant(tenantId, page, size);
        
        PageResponse<InvoiceResponse> response = PageResponse.<InvoiceResponse>builder()
                .content(invoices)
                .page(page)
                .size(size)
                .totalElements(invoices.size())
                .totalPages((int) Math.ceil((double) invoices.size() / size))
                .first(page == 0)
                .last(invoices.size() < size)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Get recent invoices for tenant", 
        description = "Retrieve recent invoices for a specific tenant",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recent invoices retrieved successfully",
                content = @Content(schema = @Schema(implementation = List.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
        }
    )
    @GetMapping("/{tenantKey}/recent")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getRecentInvoices(
            @Parameter(description = "Tenant key") @PathVariable String tenantKey) {
        
        // Convert tenant key to tenant ID
        Long tenantId = tenantService.getTenantIdByKey(tenantKey);
        if (tenantId == null) {
            throw new RuntimeException("Tenant not found with key: " + tenantKey);
        }
        
        List<InvoiceResponse> recentInvoices = invoiceService.getRecentInvoicesByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(recentInvoices));
    }

    @Operation(
        summary = "Get invoice by ID", 
        description = "Retrieve a specific invoice by ID",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoice retrieved successfully",
                content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
        }
    )
    @GetMapping("/{invoiceId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(
            @Parameter(description = "Invoice ID") @PathVariable Long invoiceId) {
        
        InvoiceResponse invoice = invoiceService.getInvoiceById(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @Operation(
        summary = "Create invoice", 
        description = "Create a new invoice",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoice created successfully",
                content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid invoice data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TENANT_OWNER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody InvoiceRequest request) {
        
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Invoice created successfully"));
    }

    @Operation(
        summary = "Update invoice status", 
        description = "Update the status of an invoice",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoice status updated successfully",
                content = @Content(schema = @Schema(implementation = InvoiceResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
        }
    )
    @PutMapping("/{invoiceId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TENANT_OWNER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoiceStatus(
            @Parameter(description = "Invoice ID") @PathVariable Long invoiceId,
            @Parameter(description = "New status") @RequestParam String status) {
        
        InvoiceResponse response = invoiceService.updateInvoiceStatus(invoiceId, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Invoice status updated successfully"));
    }

    @Operation(
        summary = "Delete invoice", 
        description = "Delete an invoice",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Invoice deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
        }
    )
    @DeleteMapping("/{invoiceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TENANT_OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(
            @Parameter(description = "Invoice ID") @PathVariable Long invoiceId) {
        
        invoiceService.deleteInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(null, "Invoice deleted successfully"));
    }

    @Operation(
        summary = "Download invoice PDF", 
        description = "Download invoice as PDF file",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "PDF downloaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
        }
    )
    @GetMapping("/{invoiceId}/download")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadInvoice(
            @Parameter(description = "Invoice ID") @PathVariable Long invoiceId) {
        
        // TODO: Implement actual PDF generation
        // For now, return empty PDF
        byte[] pdfContent = "PDF content placeholder".getBytes();
        
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=invoice-" + invoiceId + ".pdf")
                .body(pdfContent);
    }
}
