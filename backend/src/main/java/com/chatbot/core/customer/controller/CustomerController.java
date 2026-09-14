package com.chatbot.core.customer.controller;

import com.chatbot.core.customer.dto.CustomerDto;
import com.chatbot.core.customer.dto.CustomerStatsDto;
import com.chatbot.core.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs for customer data management and stats")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "Get all customers with pagination")
    public ResponseEntity<Page<CustomerDto>> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction.toUpperCase()).orElse(Sort.Direction.DESC);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        return ResponseEntity.ok(customerService.getCustomers(pageable));
    }

    @GetMapping("/{psid}")
    @Operation(summary = "Get customer details by PSID")
    public ResponseEntity<CustomerDto> getCustomerByPsid(@PathVariable String psid) {
        return ResponseEntity.ok(customerService.getCustomerByPsid(psid));
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers by keyword")
    public ResponseEntity<Page<CustomerDto>> searchCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return ResponseEntity.ok(customerService.searchCustomers(keyword, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get customers by status")
    public ResponseEntity<Page<CustomerDto>> getCustomersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return ResponseEntity.ok(customerService.getCustomersByStatus(status, pageable));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get customer statistics summary")
    public ResponseEntity<CustomerStatsDto> getCustomerStats() {
        return ResponseEntity.ok(customerService.getCustomerStats());
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get list of available customer statuses")
    public ResponseEntity<List<String>> getAvailableStatuses() {
        return ResponseEntity.ok(customerService.getAvailableStatuses());
    }
}
