package com.chatbot.spokes.odoo.controller;

import com.chatbot.spokes.odoo.dto.CustomerDataDTO;
import com.chatbot.spokes.odoo.model.CustomerStatus;
import com.chatbot.spokes.odoo.service.CustomerDataQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

/**
 * Controller cho Customer Data - Gộp thông tin từ 3 bảng
 */
@Slf4j
@RestController
@RequestMapping("/api/odoo/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Data", description = "Customer data management - Combined from staging, facebook users, and captured phones")
public class CustomerDataController {

    private final CustomerDataQueryService customerDataQueryService;

    /**
     * Lấy tất cả customer data với pagination
     */
    @GetMapping
    @Operation(
        summary = "Get all customers",
        description = "Retrieve all customer data combined from staging, facebook users, and captured phones"
    )
    public ResponseEntity<Page<CustomerDataDTO>> getAllCustomers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "updatedAt") String sort,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String direction
    ) {
        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            Page<CustomerDataDTO> customers = customerDataQueryService.getAllCustomers(pageable);
            log.info("Retrieved {} customers for page {}", customers.getTotalElements(), page);
            
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            log.error("Error retrieving customers: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy customer data theo PSID
     */
    @GetMapping("/{psid}")
    @Operation(
        summary = "Get customer by PSID",
        description = "Retrieve specific customer data by Facebook PSID"
    )
    public ResponseEntity<CustomerDataDTO> getCustomerByPsid(
            @Parameter(description = "Facebook PSID") @PathVariable String psid
    ) {
        try {
            CustomerDataDTO customer = customerDataQueryService.getCustomerByPsid(psid);
            
            if (customer == null) {
                return ResponseEntity.notFound().build();
            }
            
            log.info("Retrieved customer data for PSID: {}", psid);
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            log.error("Error retrieving customer for PSID {}: {}", psid, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Tìm kiếm customer data
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search customers",
        description = "Search customers by name or phone number"
    )
    public ResponseEntity<Page<CustomerDataDTO>> searchCustomers(
            @Parameter(description = "Search keyword (name or phone)") @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
            Page<CustomerDataDTO> customers = customerDataQueryService.searchCustomers(keyword, pageable);
            
            log.info("Search for '{}' returned {} results", keyword, customers.getTotalElements());
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            log.error("Error searching customers with keyword '{}': {}", keyword, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy customer data theo status
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get customers by status",
        description = "Retrieve customers filtered by processing status"
    )
    public ResponseEntity<Page<CustomerDataDTO>> getCustomersByStatus(
            @Parameter(description = "Customer status") @PathVariable CustomerStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
            Page<CustomerDataDTO> customers = customerDataQueryService.getCustomersByStatus(status, pageable);
            
            log.info("Retrieved {} customers with status {}", customers.getTotalElements(), status);
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            log.error("Error retrieving customers with status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy thống kê customer data
     */
    @GetMapping("/stats")
    @Operation(
        summary = "Get customer statistics",
        description = "Retrieve aggregated statistics about customer data"
    )
    public ResponseEntity<Map<String, Object>> getCustomerStats() {
        try {
            Map<String, Object> stats = customerDataQueryService.getCustomerStats();
            log.info("Retrieved customer statistics");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error retrieving customer statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy các status có sẵn
     */
    @GetMapping("/statuses")
    @Operation(
        summary = "Get available statuses",
        description = "Retrieve list of available customer statuses"
    )
    public ResponseEntity<CustomerStatus[]> getAvailableStatuses() {
        return ResponseEntity.ok(CustomerStatus.values());
    }
}
