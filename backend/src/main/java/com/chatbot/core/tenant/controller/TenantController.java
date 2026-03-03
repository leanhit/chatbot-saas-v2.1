package com.chatbot.core.tenant.controller;

import com.chatbot.core.tenant.dto.*;
import com.chatbot.core.tenant.mapper.TenantMapper;
import com.chatbot.core.tenant.service.TenantService;
import com.chatbot.core.tenant.profile.service.TenantProfileService;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.identity.model.SystemRole;
import com.chatbot.core.tenant.membership.model.TenantRole;
import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.membership.service.TenantMembershipFacade;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // Use fully qualified name to avoid conflict
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/tenants")
@Slf4j
@Tag(name = "Tenant Management", description = "Multi-tenant operations and management")
public class TenantController {

    private final TenantService tenantService;
    private final TenantProfileService tenantProfileService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TenantMemberRepository tenantMemberRepository;
    private final TenantMembershipFacade tenantMembershipFacade;

    public TenantController(TenantService tenantService, 
                          TenantProfileService tenantProfileService, 
                          TenantRepository tenantRepository,
                          UserRepository userRepository,
                          TenantMemberRepository tenantMemberRepository,
                          TenantMembershipFacade tenantMembershipFacade) {
        this.tenantService = tenantService;
        this.tenantProfileService = tenantProfileService;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.tenantMemberRepository = tenantMemberRepository;
        this.tenantMembershipFacade = tenantMembershipFacade;
    }

    /**
     * Lấy danh sách tenant đầy đủ thông tin để hiển thị lựa chọn (Profile, Address)
     */
    @GetMapping("/me")
    @Operation(
        summary = "Get user tenants",
        description = "Retrieve all tenants associated with the authenticated user, including profile and address information.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenants retrieved successfully",
                content = @Content(schema = @Schema(implementation = TenantDetailResponse.class)))
        }
    )
    public List<TenantDetailResponse> getUserTenants() {
        return tenantService.getUserTenantsDetail();
    }

    /**
     * Tạo tenant mới.
     */
    @PostMapping
    @Operation(
        summary = "Create new tenant",
        description = "Create a new tenant with the specified details. The user creating the tenant becomes the owner.",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant created successfully",
                content = @Content(schema = @Schema(implementation = TenantResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tenant name already exists")
        }
    )
    public TenantResponse create(
            @Parameter(description = "Tenant creation details", required = true)
            @RequestBody CreateTenantRequest request) {
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

    /**
     * Cập nhật logo tenant.
     */
    @PutMapping("/key/{tenantKey}/logo")
    @Operation(
        summary = "Update tenant logo",
        description = "Upload and update tenant logo image",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logo updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tenant not found")
        }
    )
    public TenantResponse updateLogo(
            @PathVariable String tenantKey,
            @Parameter(description = "Logo image file", required = true)
            @RequestParam("logo") org.springframework.web.multipart.MultipartFile file
    ) {
        try {
            // Validate tenantKey format
            if (tenantKey == null || tenantKey.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant key không được để trống");
            }
            
            // Check if tenantKey looks like [object File] or other invalid patterns
            if (tenantKey.contains("[object") || tenantKey.contains("undefined") || tenantKey.length() < 10) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Tenant key không hợp lệ. Expected UUID format, received: " + tenantKey);
            }
            
            // Validate file
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File không được để trống");
            }
            
            // Validate file content (check if it's actual image data, not "undefined")
            if (file.getSize() == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File không có nội dung");
            }
            
            // Validate file type (only allow images)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Chỉ chấp nhận file ảnh. Received: " + contentType);
            }
            
            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "File quá lớn. Kích thước tối đa: 5MB");
            }
            
            log.info("📤 [TenantController] Updating logo for tenantKey: {}, fileName: {}, fileSize: {}", 
                    tenantKey, file.getOriginalFilename(), file.getSize());
            
            // Convert tenantKey to tenantId
            Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Tenant không tồn tại với key: " + tenantKey));
            
            // Check authorization - user must be OWNER or ADMIN
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            
            boolean isAdmin = userRepository.findByEmail(currentUserEmail)
                    .map(user -> user.getSystemRole() == SystemRole.ADMIN)
                    .orElse(false);
                    
            boolean isOwner = tenantMemberRepository.findByTenantIdAndUserEmailAndStatus(
                    tenant.getId(), 
                    currentUserEmail, 
                    MembershipStatus.ACTIVE
            ).map(member -> member.getRole() == TenantRole.OWNER)
            .orElse(false);
            
            if (!isAdmin && !isOwner) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Bạn không có quyền cập nhật logo tenant này");
            }
            
            // Update logo using TenantProfileService
            tenantProfileService.updateLogo(tenant.getId(), file);
            
            log.info("✅ [TenantController] Logo updated successfully for tenant: {}", tenantKey);
            
            // Return tenant response directly to avoid redundant query
            return TenantMapper.toResponse(tenant);
            
        } catch (ResponseStatusException e) {
            // Re-throw ResponseStatusException as-is
            throw e;
        } catch (Exception e) {
            log.error("❌ [TenantController] Failed to update tenant logo: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Không thể cập nhật logo: " + e.getMessage(), e);
        }
    }

    /**
     * Get tenant members
     */
    @GetMapping("/key/{tenantKey}/members")
    public Object getTenantMembers(@PathVariable String tenantKey, Pageable pageable) {
        try {
            // Convert tenantKey to tenantId and delegate to membership facade
            Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
            
            // Delegate to TenantMembershipFacade
            return tenantMembershipFacade.listMembers(tenant.getId(), pageable);
        } catch (Exception e) {
            log.error("Failed to get tenant members: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to get tenant members: " + e.getMessage(), e);
        }
    }

    /**
     * Get tenant join requests
     */
    @GetMapping("/key/{tenantKey}/members/join-requests")
    public Object getJoinRequests(@PathVariable String tenantKey) {
        try {
            // Convert tenantKey to tenantId and delegate to membership facade
            Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
            
            // Delegate to TenantMembershipFacade
            return tenantMembershipFacade.pending(tenant.getId());
        } catch (Exception e) {
            log.error("Failed to get join requests: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to get join requests: " + e.getMessage(), e);
        }
    }

    /**
     * Get tenant invitations
     */
    @GetMapping("/key/{tenantKey}/invitations")
    public Object getInvitations(@PathVariable String tenantKey) {
        try {
            // Convert tenantKey to tenantId and delegate to invitation service
            Long tenantId = tenantMembershipFacade.getTenantIdByKey(tenantKey);
            
            // Delegate to TenantInvitationService through facade
            return tenantMembershipFacade.getInvitations(tenantId);
        } catch (Exception e) {
            log.error("Failed to get invitations: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to get invitations: " + e.getMessage(), e);
        }
    }

    /**
     * Update member role
     */
    @PutMapping("/key/{tenantKey}/members/{userId}/role")
    public void updateMemberRole(@PathVariable String tenantKey, @PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            Long tenantId = tenantMembershipFacade.getTenantIdByKey(tenantKey);
            String role = request.get("role");
            tenantMembershipFacade.updateRole(tenantId, userId, com.chatbot.core.tenant.membership.model.TenantRole.valueOf(role));
        } catch (Exception e) {
            log.error("Failed to update member role: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to update member role: " + e.getMessage(), e);
        }
    }

    /**
     * Remove member
     */
    @DeleteMapping("/key/{tenantKey}/members/{userId}")
    public void removeMember(@PathVariable String tenantKey, @PathVariable Long userId) {
        try {
            Long tenantId = tenantMembershipFacade.getTenantIdByKey(tenantKey);
            tenantMembershipFacade.removeMember(tenantId, userId);
        } catch (Exception e) {
            log.error("Failed to remove member: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to remove member: " + e.getMessage(), e);
        }
    }

    /**
     * Update join request status
     */
    @PatchMapping("/key/{tenantKey}/members/join-requests/{requestId}")
    public void updateJoinRequest(@PathVariable String tenantKey, @PathVariable Long requestId, @RequestBody Map<String, String> request) {
        try {
            Long tenantId = tenantMembershipFacade.getTenantIdByKey(tenantKey);
            String status = request.get("status");
            tenantMembershipFacade.updateJoinRequest(tenantId, requestId, com.chatbot.core.tenant.membership.model.MembershipStatus.valueOf(status));
        } catch (Exception e) {
            log.error("Failed to update join request: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to update join request: " + e.getMessage(), e);
        }
    }
}
