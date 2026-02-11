package com.chatbot.core.tenant.core.controller;

import com.chatbot.core.tenant.core.dto.*;
import com.chatbot.core.tenant.core.service.TenantService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
@Slf4j
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * Lấy danh sách tenant đầy đủ thông tin để hiển thị lựa chọn (Profile, Address)
     */
    @GetMapping("/me")
    public List<TenantDetailResponse> getUserTenants() {
        return tenantService.getUserTenantsDetail();
    }

    /**
     * Tạo tenant mới.
     */
    @PostMapping
    public TenantResponse create(@RequestBody CreateTenantRequest request) {
        log.info("🏗️ [TenantController] Starting tenant creation");
        log.info("📋 [TenantController] Request data: name={}, visibility={}", request.getName(), request.getVisibility());
        
        try {
            TenantResponse response = tenantService.createTenant(request);
            log.info("✅ [TenantController] Tenant created successfully: tenantKey={}, name={}", 
                    response.getTenantKey(), response.getName());
            return response;
        } catch (Exception e) {
            log.error("❌ [TenantController] Failed to create tenant: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Suspend tenant (OWNER).
     */
    @PostMapping("/{id}/suspend")
    public void suspend(@PathVariable Long id) {
        tenantService.suspendTenant(id);
    }

    /**
     * Activate tenant.
     */
    @PostMapping("/{id}/activate")
    public void activate(@PathVariable Long id) {
        tenantService.activateTenant(id);
    }

    /**
     * Lấy chi tiết tenant (user phải là member).
     */
    @GetMapping("/{id}")
    public TenantResponse getTenantById(@PathVariable Long id) {
        return tenantService.getTenantForCurrentUser(id);
    }
    
    /**
     * Lấy đầy đủ thông tin tenant (core + profile + addresses)
     */
    @GetMapping("/{id}/full")
    public TenantDetailResponse getTenantDetail(@PathVariable Long id) {
        return tenantService.getTenantDetail(id);
    }

    /**
     * Lấy đầy đủ thông tin tenant bằng tenantKey (cho frontend)
     */
    @GetMapping("/key/{tenantKey}/full")
    public TenantDetailResponse getTenantDetailByTenantKey(@PathVariable String tenantKey) {
        return tenantService.getTenantDetailByTenantKey(tenantKey);
    }

    /**
     * Switch tenant hiện tại.
     */
    @PostMapping("/{id}/switch")
    public TenantResponse switchTenant(@PathVariable Long id) {
        return tenantService.switchTenant(id);
    }

    /**
     * Switch tenant hiện tại bằng tenantKey.
     */
    @PostMapping("/key/{tenantKey}/switch")
    public TenantResponse switchTenantByKey(@PathVariable String tenantKey) {
        return tenantService.switchTenantByKey(tenantKey);
    }

    /**
     * Search tenant.
     */
    @GetMapping("/search")
    public Page<TenantSearchResponse> searchTenants(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        String currentUserEmail =
                SecurityContextHolder.getContext().getAuthentication().getName();

        TenantSearchRequest request = new TenantSearchRequest();
        request.setKeyword(keyword);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDirection(sortDirection);

        return tenantService.searchTenants(request, currentUserEmail);
    }

    /**
     * Cập nhật thông tin cơ bản của tenant (Tên, Trạng thái, Hạn dùng).
     */
    @PutMapping("/key/{tenantKey}")
    public TenantResponse updateBasicInfo(
            @PathVariable String tenantKey,
            @RequestBody TenantBasicInfoRequest request // Đảm bảo DTO này có name, status, expiresAt
    ) {
        return tenantService.updateBasicInfo(tenantKey, request);
    }

    /**
     * Cập nhật thông tin liên hệ của tenant.
     */
    @PutMapping("/key/{tenantKey}/contact")
    public TenantResponse updateContactInfo(
            @PathVariable String tenantKey,
            @RequestBody Map<String, Object> contactData
    ) {
        return tenantService.updateContactInfo(tenantKey, contactData);
    }
}
