package com.chatbot.core.tenant.service;

import com.chatbot.shared.address.model.OwnerType;
import com.chatbot.shared.address.service.AddressService;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import com.chatbot.core.tenant.exception.*;

import java.util.stream.Collectors;

import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.dto.*;
import com.chatbot.core.tenant.mapper.TenantMapper;
import com.chatbot.core.tenant.model.*;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.membership.service.TenantMembershipFacade;
import com.chatbot.core.tenant.membership.model.TenantRole;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.membership.model.TenantMember;
import com.chatbot.core.tenant.profile.model.TenantProfile;
import com.chatbot.core.tenant.profile.dto.TenantProfileResponse;
import com.chatbot.core.tenant.profile.repository.TenantProfileRepository;
import com.chatbot.core.user.repository.AuthRepository;
import com.chatbot.core.tenant.profile.service.TenantProfileService;

import com.chatbot.shared.address.dto.AddressDetailResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final TenantMemberRepository tenantMemberRepository;
    private final TenantMembershipFacade tenantMembershipFacade;
    private final TenantPackageService tenantPackageService;
    private final TenantProfileRepository tenantProfileRepository;
    private final TenantProfileService tenantProfileService;
    private final AddressService addressService;
    private final TenantAuditLogService auditLogService;
    private final TenantPermissionValidator permissionValidator;

    @Value("${tenant.trial.days:30}")
    private int trialDays;

    // =========================================================================
    // HELPERS
    // =========================================================================

    // =========================================================================
    // CREATE
    // =========================================================================

    @Transactional(rollbackFor = Exception.class)
    public TenantResponse createTenant(CreateTenantRequest request) {
        log.info("[TenantService] Starting tenant creation");

        String currentUserEmail = permissionValidator.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Tenant tenant = TenantMapper.toEntity(request, trialDays);
        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("[TenantService] Tenant saved: id={}, key={}", savedTenant.getId(), savedTenant.getTenantKey());

        // Tạo membership OWNER
        TenantMember owner = TenantMember.builder()
                .tenant(savedTenant)
                .userId(currentUser.getId()) // Application-level join: store userId instead of User object
                .role(TenantRole.OWNER)
                .status(MembershipStatus.ACTIVE)
                .build();
        tenantMemberRepository.save(owner);

        // Profile creation removed - will be created via separate API call to avoid transaction rollback issues
        log.info("[TenantService] Skipping profile creation for tenant: {}", savedTenant.getId());

        // Tạo địa chỉ rỗng - non-critical
        try {
            createEmptyAddressForTenant(savedTenant.getId());
            log.info("[TenantService] Created empty address for tenant: {}", savedTenant.getId());
        } catch (Exception e) {
            log.error("[TenantService] [MONITORING] Failed to create address for tenant {}: {} - Tenant created but without address. Manual intervention may be required.", 
                    savedTenant.getId(), e.getMessage(), e);
            // Address creation is non-critical, continue with tenant creation
            // Note: Consider integrating with monitoring/alerting systems (Prometheus Alertmanager, PagerDuty, etc.)
            // for production environments to notify operations team of such failures
        }

        // Gán gói mặc định — critical operation, rollback if fails
        try {
            tenantPackageService.assignDefaultPackageToTenant(savedTenant);
        } catch (Exception e) {
            log.error("[TenantService] Failed to assign default package to {}: {}", 
                    savedTenant.getTenantKey(), e.getMessage(), e);
            throw new BusinessLogicException("Failed to assign default package. Tenant creation rolled back: " + e.getMessage());
        }

        auditLogService.logAction(savedTenant.getId(), currentUserEmail, "CREATE_TENANT",
                "Tenant created with key " + savedTenant.getTenantKey());

        log.info("[TenantService] Tenant creation complete: key={}", savedTenant.getTenantKey());
        return TenantMapper.toResponse(savedTenant);
    }

    // =========================================================================
    // READ
    // =========================================================================

    @Cacheable(value = "tenants", key = "#tenantId", unless = "#result == null")
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public Tenant getTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with ID: " + tenantId));
    }

    @Cacheable(value = "tenant-key-to-id", key = "#tenantKey", unless = "#result == null")
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public Long getTenantIdByKey(String tenantKey) {
        return tenantRepository.findByTenantKey(tenantKey)
                .map(Tenant::getId)
                .orElse(null);
    }

    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public TenantResponse getTenantForCurrentUser(Long tenantId) {
        String currentUserEmail = permissionValidator.getCurrentUserEmail();
        Long userId = authRepository.findByEmail(currentUserEmail)
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        TenantMember member = tenantMemberRepository
                .findByTenantIdAndUserIdAndStatus(tenantId, userId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new InsufficientPermissionException("Bạn không có quyền truy cập tenant này"));
        return TenantMapper.toResponse(member.getTenant());
    }

    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public TenantDetailResponse getTenantDetail(Long tenantId) {
        TenantResponse tenantResponse = getTenantForCurrentUser(tenantId);
        TenantProfileResponse profile = tenantProfileService.getProfile(tenantId);

        AddressDetailResponseDTO addressDetail = null;
        try {
            addressDetail = addressService.getSingleAddressByOwner(tenantId, OwnerType.TENANT, tenantId);
        } catch (RuntimeException e) {
            // Tenant chưa có địa chỉ — bỏ qua
        }

        return TenantDetailResponse.from(tenantResponse, profile, addressDetail);
    }

    /**
     * Lấy chi tiết tenant theo tenantKey.
     * PUBLIC tenant: bất kỳ user đã đăng nhập đều xem được.
     * PRIVATE tenant: chỉ member mới xem được.
     * NOTE: No @Transactional because this method uses cross-datasource operations (tenant, user, shared)
     */
    public TenantDetailResponse getTenantDetailByTenantKey(String tenantKey) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with key: " + tenantKey));

        log.info("📝 [DEBUG] getTenantDetailByTenantKey - tenantKey: {}, tenantId: {}, currentPackageId: {}",
                tenantKey, tenant.getId(), tenant.getCurrentPackageId());

        // Kiểm tra quyền truy cập cho PRIVATE tenant
        if (tenant.getVisibility() == TenantVisibility.PRIVATE) {
            String currentUserEmail = permissionValidator.getCurrentUserEmail();
            Long userId = authRepository.findByEmail(currentUserEmail)
                    .map(User::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            boolean isMember = tenantMemberRepository
                    .findByTenantIdAndUserIdAndStatus(tenant.getId(), userId, MembershipStatus.ACTIVE)
                    .isPresent();
            if (!isMember) {
                throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_ACCESS_TENANT, "You do not have permission to access this tenant");
            }
        }

        TenantResponse tenantResponse = TenantMapper.toResponse(tenant);
        
        TenantProfileResponse profile = null;
        try {
            profile = tenantProfileService.getProfile(tenant.getId());
        } catch (ResponseStatusException e) {
            // Tenant chưa có profile — bỏ qua
            log.info("[TenantService] Profile not found for tenant {}, treating as null", tenant.getId());
        } catch (Exception e) {
            log.error("[TenantService] Unexpected error getting profile for tenant {}: {}", tenant.getId(), e.getMessage(), e);
            throw e;
        }

        AddressDetailResponseDTO addressDetail = null;
        try {
            addressDetail = addressService.getSingleAddressByOwner(tenant.getId(), OwnerType.TENANT, tenant.getId());
        } catch (RuntimeException e) {
            // Tenant chưa có địa chỉ — bỏ qua
            log.info("[TenantService] Address not found for tenant {}, treating as null", tenant.getId());
        }

        return TenantDetailResponse.from(tenantResponse, profile, addressDetail);
    }

    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public List<TenantDetailResponse> getUserTenantsDetail() {
        String currentUserEmail = permissionValidator.getCurrentUserEmail();
        Long userId = authRepository.findByEmail(currentUserEmail)
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Tenant> tenants = tenantMemberRepository
                .findByUserIdWithTenant(userId)
                .stream()
                .map(TenantMember::getTenant)
                .collect(Collectors.toList());

        if (tenants.isEmpty()) return Collections.emptyList();

        List<Long> tenantIds = tenants.stream().map(Tenant::getId).collect(Collectors.toList());
        Map<Long, TenantProfileResponse> profiles = tenantProfileService.getProfilesByTenantIds(tenantIds);

        return tenants.stream().map(tenant -> TenantDetailResponse.from(
                TenantMapper.toResponse(tenant),
                profiles.get(tenant.getId()),
                Collections.emptyList()
        )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public Page<TenantSearchResponse> searchTenants(TenantSearchRequest request, String currentUserEmail) {
        Page<Tenant> tenantsPage = tenantRepository.findByVisibilityAndStatusAndNameContainingIgnoreCase(
                TenantVisibility.PUBLIC,
                TenantStatus.ACTIVE,
                request.getKeyword() != null ? request.getKeyword() : "",
                request.toPageable()
        );

        Long userId = authRepository.findByEmail(currentUserEmail)
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<Long> tenantIds = tenantsPage.getContent().stream()
                .map(Tenant::getId).collect(Collectors.toList());
        
        // Fix N+1 query: fetch only memberships for the tenants in the current page
        List<TenantMember> userMemberships = tenantIds.isEmpty() 
                ? Collections.emptyList() 
                : tenantMemberRepository.findByUserIdAndTenantIdIn(userId, tenantIds);

        Map<Long, com.chatbot.core.tenant.profile.dto.TenantProfileResponse> profilesMap =
                tenantProfileService.getProfilesByTenantIds(tenantIds);

        return tenantsPage.map(tenant -> {
            TenantMembershipStatus status = userMemberships.stream()
                    .filter(m -> m.getTenant().getId().equals(tenant.getId()))
                    .findFirst()
                    .map(m -> {
                        if (m.getStatus() == MembershipStatus.PENDING)  return TenantMembershipStatus.PENDING;
                        if (m.getStatus() == MembershipStatus.ACTIVE)   return TenantMembershipStatus.APPROVED;
                        return TenantMembershipStatus.NONE;
                    })
                    .orElse(TenantMembershipStatus.NONE);

            com.chatbot.core.tenant.profile.dto.TenantProfileResponse profile = profilesMap.get(tenant.getId());

            // Fetch province from address if available
            String province = "";
            try {
                AddressDetailResponseDTO address = addressService.getSingleAddressByOwner(tenant.getId(), OwnerType.TENANT, tenant.getId());
                if (address != null && address.getProvince() != null) {
                    province = address.getProvince();
                }
            } catch (Exception e) {
                // Address not available, leave province empty
            }

            return TenantSearchResponse.builder()
                    .id(tenant.getId())
                    .tenantKey(tenant.getTenantKey())
                    .name(tenant.getName())
                    .status(tenant.getStatus())
                    .visibility(tenant.getVisibility())
                    .createdAt(tenant.getCreatedAt())
                    .membershipStatus(status)
                    .logoUrl(profile != null ? profile.getLogoUrl() : null)
                    .contactEmail(profile != null ? profile.getContactEmail() : null)
                    .province(province)
                    .build();
        });
    }

    // =========================================================================
    // STATUS TRANSITIONS
    // =========================================================================

    @CacheEvict(value = "tenants", key = "#tenantId")
    @Transactional(transactionManager = "tenantTransactionManager")
    public void suspendTenant(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        String currentUserEmail = permissionValidator.getCurrentUserEmail();

        if (!permissionValidator.isAdmin(currentUserEmail)) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_SUSPEND_TENANT, "Only admin can suspend tenant");
        }

        validateStatusTransition(tenant.getStatus(), TenantStatus.SUSPENDED);
        tenant.setStatus(TenantStatus.SUSPENDED);
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantRepository.save(tenant);

        auditLogService.logAction(tenantId, currentUserEmail, "SUSPEND_TENANT", "Tenant suspended");
        log.info("[TenantService] Tenant {} suspended by {}", tenantId, currentUserEmail);
    }

    @CacheEvict(value = "tenants", key = "#tenantId")
    @Transactional(transactionManager = "tenantTransactionManager")
    public void activateTenant(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        String currentUserEmail = permissionValidator.getCurrentUserEmail();

        if (!permissionValidator.isAdmin(currentUserEmail)) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_RESUME_TENANT, "Only admin can activate tenant");
        }

        validateStatusTransition(tenant.getStatus(), TenantStatus.ACTIVE);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantRepository.save(tenant);

        auditLogService.logAction(tenantId, currentUserEmail, "ACTIVATE_TENANT", "Tenant activated");
        log.info("[TenantService] Tenant {} activated by {}", tenantId, currentUserEmail);
    }

    @CacheEvict(value = "tenants", key = "#tenantId")
    @Transactional(transactionManager = "tenantTransactionManager")
    public void deactivateTenant(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        String currentUserEmail = permissionValidator.getCurrentUserEmail();

        if (!permissionValidator.isOwner(tenantId, currentUserEmail)) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_DELETE_TENANT, "Only owner can deactivate tenant");
        }

        validateStatusTransition(tenant.getStatus(), TenantStatus.INACTIVE);
        tenant.setStatus(TenantStatus.INACTIVE);
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantRepository.save(tenant);

        auditLogService.logAction(tenantId, currentUserEmail, "DEACTIVATE_TENANT", "Tenant deactivated");
        log.info("[TenantService] Tenant {} deactivated by {}", tenantId, currentUserEmail);
    }

    /**
     * Soft-delete tenant — chỉ ADMIN hoặc OWNER.
     */
    @CacheEvict(value = "tenants", key = "#tenantId")
    @Transactional(transactionManager = "tenantTransactionManager")
    public void deleteTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with ID: " + tenantId));

        String currentUserEmail = permissionValidator.getCurrentUserEmail();

        // Defense-in-depth: kiểm tra quyền ngay tại tầng service
        if (!permissionValidator.isAdminOrOwner(tenantId, currentUserEmail)) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_DELETE_TENANT, "Only ADMIN or OWNER can delete tenant");
        }

        tenant.setStatus(TenantStatus.DELETED);
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantRepository.save(tenant);

        auditLogService.logAction(tenantId, currentUserEmail, "DELETE_TENANT", "Tenant soft-deleted");
        log.info("[TenantService] Tenant {} soft-deleted by {}", tenantId, currentUserEmail);
    }

    // =========================================================================
    // SWITCH
    // =========================================================================

    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public TenantResponse switchTenant(Long tenantId) {
        return getTenantForCurrentUser(tenantId);
    }

    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public TenantResponse switchTenantByKey(String tenantKey) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with key: " + tenantKey));
        return getTenantForCurrentUser(tenant.getId());
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    @Caching(evict = {
        @CacheEvict(value = "tenants", key = "#result.id", condition = "#result != null"),
        @CacheEvict(value = "tenant-key-to-id", key = "#tenantKey")
    })
    @Transactional(rollbackFor = Exception.class, transactionManager = "tenantTransactionManager")
    public TenantResponse updateBasicInfo(String tenantKey, TenantBasicInfoRequest req) {
        if (tenantKey == null || tenantKey.trim().isEmpty()) {
            throw new InvalidTenantKeyException("Tenant key cannot be empty");
        }

        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with key: " + tenantKey));

        String currentUserEmail = permissionValidator.getCurrentUserEmail();
        if (!permissionValidator.isAdminOrOwner(tenant.getId(), currentUserEmail)) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_MANAGE_MEMBERS, "You do not have permission to update tenant information");
        }

        if (req.getName() != null && !req.getName().trim().isEmpty()) {
            tenant.setName(req.getName().trim());
        }
        if (req.getStatus() != null) {
            validateStatusTransition(tenant.getStatus(), req.getStatus());
            tenant.setStatus(req.getStatus());
        }
        if (req.getVisibility() != null) {
            tenant.setVisibility(req.getVisibility());
        }
        // NOTE: expiresAt is intentionally NOT updated here.
        // It is managed exclusively by TenantPackageService via the payment flow.
        // Allowing direct modification would bypass payment validation and the cap logic.

        tenantRepository.save(tenant);
        auditLogService.logAction(tenant.getId(), currentUserEmail, "UPDATE_BASIC_INFO",
                "Updated basic info for tenant " + tenantKey);
        return TenantMapper.toResponse(tenant);
    }

    /**
     * Cập nhật thông tin liên hệ. Dùng typed DTO thay vì Map<String,Object>.
     */
    @Caching(evict = {
        @CacheEvict(value = "tenants", key = "#result.id", condition = "#result != null"),
        @CacheEvict(value = "tenant-key-to-id", key = "#tenantKey")
    })
    @Transactional(rollbackFor = Exception.class, transactionManager = "tenantTransactionManager")
    public TenantResponse updateContactInfo(String tenantKey, TenantContactInfoRequest req) {
        if (tenantKey == null || tenantKey.trim().isEmpty()) {
            throw new InvalidTenantKeyException("Tenant key cannot be empty");
        }

        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with key: " + tenantKey));

        String currentUserEmail = permissionValidator.getCurrentUserEmail();
        if (!permissionValidator.isAdminOrOwner(tenant.getId(), currentUserEmail)) {
            throw new InsufficientPermissionException(com.chatbot.shared.exceptions.ErrorCode.CANNOT_MANAGE_MEMBERS, "You do not have permission to update tenant contact information");
        }

        TenantProfile profile = tenantProfileRepository.findByTenant_Id(tenant.getId())
                .orElseGet(() -> {
                    TenantProfile p = new TenantProfile();
                    p.setTenant(tenant);
                    return tenantProfileRepository.save(p);
                });

        if (req.getEmail() != null && !req.getEmail().trim().isEmpty()) {
            profile.setContactEmail(req.getEmail().trim());
        }
        if (req.getPhone() != null && !req.getPhone().trim().isEmpty()) {
            profile.setContactPhone(req.getPhone().trim());
        }
        if (req.getWebsite() != null && !req.getWebsite().trim().isEmpty()) {
            profile.setWebsite(req.getWebsite().trim());
        }

        tenantProfileRepository.save(profile);
        auditLogService.logAction(tenant.getId(), currentUserEmail, "UPDATE_CONTACT_INFO",
                "Updated contact info for tenant " + tenantKey);
        return TenantMapper.toResponseWithProfile(tenant, profile);
    }

    // =========================================================================
    // BULK INVITE
    // =========================================================================

    @Transactional(transactionManager = "tenantTransactionManager")
    public List<InvitationResponse> bulkInviteUsers(String tenantKey, List<BulkInvitationRequest.Invitation> invitations) {
        String currentUserEmail = permissionValidator.getCurrentUserEmail();

        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));

        if (!permissionValidator.isAdminOrOwner(tenant.getId(), currentUserEmail)) {
            throw new InsufficientPermissionException("Insufficient permission for bulk invitation");
        }

        User adminUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new InsufficientPermissionException("User not found"));

        List<InvitationResponse> results = new java.util.ArrayList<>();
        for (BulkInvitationRequest.Invitation invite : invitations) {
            try {
                tenantMembershipFacade.createInvitation(tenant.getId(), invite.getEmail(), invite.getRole(), adminUser);
                results.add(new InvitationResponse(invite.getEmail(), "SENT"));
                auditLogService.logAction(tenant.getId(), currentUserEmail, "BULK_INVITE",
                        "Invited " + invite.getEmail());
            } catch (Exception e) {
                log.error("[TenantService] Bulk invite failed for {}: {}", invite.getEmail(), e.getMessage());
                results.add(new InvitationResponse(invite.getEmail(), "FAILED: " + e.getMessage()));
            }
        }
        return results;
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private void createEmptyAddressForTenant(Long tenantId) {
        addressService.getOrCreateSingleAddress(tenantId, OwnerType.TENANT, tenantId);
        log.info("[TenantService] Created empty address for tenant: {}", tenantId);
    }

    private void validateStatusTransition(TenantStatus currentStatus, TenantStatus newStatus) {
        if (currentStatus == newStatus) return; // idempotent

        boolean valid = switch (currentStatus) {
            case ACTIVE    -> newStatus == TenantStatus.SUSPENDED || newStatus == TenantStatus.INACTIVE;
            case SUSPENDED -> newStatus == TenantStatus.ACTIVE;
            case INACTIVE  -> newStatus == TenantStatus.ACTIVE;
            default        -> false;
        };

        if (!valid) {
            throw new TenantStatusTransitionException(
                "Không thể chuyển từ trạng thái " + currentStatus + " sang " + newStatus);
        }
    }
}
