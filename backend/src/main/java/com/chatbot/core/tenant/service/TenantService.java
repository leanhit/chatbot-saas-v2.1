package com.chatbot.core.tenant.service;

import com.chatbot.shared.address.model.OwnerType;
import com.chatbot.shared.address.service.AddressService;
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
import com.chatbot.core.tenant.profile.service.TenantProfileService;

import com.chatbot.shared.address.dto.AddressDetailResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
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

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new InsufficientPermissionException("User not authenticated");
        }
        return auth.getName();
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    @Transactional(rollbackFor = Exception.class)
    public TenantResponse createTenant(CreateTenantRequest request) {
        log.info("[TenantService] Starting tenant creation");

        String currentUserEmail = getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));

        Tenant tenant = TenantMapper.toEntity(request, trialDays);
        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("[TenantService] Tenant saved: id={}, key={}", savedTenant.getId(), savedTenant.getTenantKey());

        // Tạo membership OWNER
        TenantMember owner = TenantMember.builder()
                .tenant(savedTenant)
                .user(currentUser)
                .role(TenantRole.OWNER)
                .status(MembershipStatus.ACTIVE)
                .build();
        tenantMemberRepository.save(owner);

        // Tạo profile rỗng
        try {
            createEmptyTenantProfile(savedTenant);
        } catch (Exception e) {
            log.error("[TenantService] Failed to create profile for tenant {}: {}", savedTenant.getId(), e.getMessage(), e);
            throw new TenantProfileException("Không thể tạo tenant profile: " + e.getMessage(), e);
        }

        // Tạo địa chỉ rỗng
        try {
            createEmptyAddressForTenant(savedTenant.getId());
        } catch (Exception e) {
            log.error("[TenantService] Failed to create address for tenant {}: {}", savedTenant.getId(), e.getMessage(), e);
            throw new TenantProfileException("Không thể tạo địa chỉ tenant: " + e.getMessage(), e);
        }

        // Gán gói mặc định — lỗi ở đây không rollback tenant creation
        try {
            tenantPackageService.assignDefaultPackageToTenant(savedTenant);
        } catch (Exception e) {
            log.error("[TenantService] Failed to assign default package to {}: {}", savedTenant.getTenantKey(), e.getMessage());
        }

        auditLogService.logAction(savedTenant.getId(), currentUserEmail, "CREATE_TENANT",
                "Tenant created with key " + savedTenant.getTenantKey());

        log.info("[TenantService] Tenant creation complete: key={}", savedTenant.getTenantKey());
        return TenantMapper.toResponse(savedTenant);
    }

    // =========================================================================
    // READ
    // =========================================================================

    @Transactional(readOnly = true)
    public Tenant getTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with ID: " + tenantId));
    }

    @Transactional(readOnly = true)
    public Long getTenantIdByKey(String tenantKey) {
        return tenantRepository.findByTenantKey(tenantKey)
                .map(Tenant::getId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public TenantResponse getTenantForCurrentUser(Long tenantId) {
        String currentUserEmail = getCurrentUserEmail();
        TenantMember member = tenantMemberRepository
                .findByTenantIdAndUserEmailAndStatus(tenantId, currentUserEmail, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new InsufficientPermissionException("Bạn không có quyền truy cập tenant này"));
        return TenantMapper.toResponse(member.getTenant());
    }

    @Transactional(readOnly = true)
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
     */
    @Transactional(readOnly = true)
    public TenantDetailResponse getTenantDetailByTenantKey(String tenantKey) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with key: " + tenantKey));

        // Kiểm tra quyền truy cập cho PRIVATE tenant
        if (tenant.getVisibility() == TenantVisibility.PRIVATE) {
            String currentUserEmail = getCurrentUserEmail();
            boolean isMember = tenantMemberRepository
                    .findByTenantIdAndUserEmailAndStatus(tenant.getId(), currentUserEmail, MembershipStatus.ACTIVE)
                    .isPresent();
            if (!isMember) {
                throw new InsufficientPermissionException("Bạn không có quyền truy cập tenant này");
            }
        }

        TenantResponse tenantResponse = TenantMapper.toResponse(tenant);
        TenantProfileResponse profile = tenantProfileService.getProfile(tenant.getId());

        AddressDetailResponseDTO addressDetail = null;
        try {
            addressDetail = addressService.getSingleAddressByOwner(tenant.getId(), OwnerType.TENANT, tenant.getId());
        } catch (RuntimeException e) {
            // Tenant chưa có địa chỉ — bỏ qua
        }

        return TenantDetailResponse.from(tenantResponse, profile, addressDetail);
    }

    @Transactional(readOnly = true)
    public List<TenantDetailResponse> getUserTenantsDetail() {
        String currentUserEmail = getCurrentUserEmail();

        List<Tenant> tenants = tenantMemberRepository
                .findByUserEmailWithTenant(currentUserEmail)
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

    @Transactional(readOnly = true)
    public Page<TenantSearchResponse> searchTenants(TenantSearchRequest request, String currentUserEmail) {
        Page<Tenant> tenantsPage = tenantRepository.findByVisibilityAndStatusAndNameContainingIgnoreCase(
                TenantVisibility.PUBLIC,
                TenantStatus.ACTIVE,
                request.getKeyword() != null ? request.getKeyword() : "",
                request.toPageable()
        );

        List<TenantMember> userMemberships = tenantMemberRepository.findByUserEmail(currentUserEmail);

        List<Long> tenantIds = tenantsPage.getContent().stream()
                .map(Tenant::getId).collect(Collectors.toList());
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
                    .province("")
                    .build();
        });
    }

    // =========================================================================
    // STATUS TRANSITIONS
    // =========================================================================

    @Transactional
    public void suspendTenant(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        String currentUserEmail = getCurrentUserEmail();

        if (!permissionValidator.isAdmin(currentUserEmail)) {
            throw new InsufficientPermissionException("Chỉ admin mới có quyền tạm dừng tenant");
        }

        validateStatusTransition(tenant.getStatus(), TenantStatus.SUSPENDED);
        tenant.setStatus(TenantStatus.SUSPENDED);
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantRepository.save(tenant);

        auditLogService.logAction(tenantId, currentUserEmail, "SUSPEND_TENANT", "Tenant suspended");
        log.info("[TenantService] Tenant {} suspended by {}", tenantId, currentUserEmail);
    }

    @Transactional
    public void activateTenant(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        String currentUserEmail = getCurrentUserEmail();

        if (!permissionValidator.isAdmin(currentUserEmail)) {
            throw new InsufficientPermissionException("Chỉ admin mới có quyền kích hoạt tenant");
        }

        validateStatusTransition(tenant.getStatus(), TenantStatus.ACTIVE);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setUpdatedAt(LocalDateTime.now());
        tenantRepository.save(tenant);

        auditLogService.logAction(tenantId, currentUserEmail, "ACTIVATE_TENANT", "Tenant activated");
        log.info("[TenantService] Tenant {} activated by {}", tenantId, currentUserEmail);
    }

    @Transactional
    public void deactivateTenant(Long tenantId) {
        Tenant tenant = getTenant(tenantId);
        String currentUserEmail = getCurrentUserEmail();

        if (!permissionValidator.isOwner(tenantId, currentUserEmail)) {
            throw new InsufficientPermissionException("Chỉ chủ sở hữu mới có quyền vô hiệu hóa tenant");
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
    @Transactional
    public void deleteTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with ID: " + tenantId));

        String currentUserEmail = getCurrentUserEmail();

        // Defense-in-depth: kiểm tra quyền ngay tại tầng service
        if (!permissionValidator.isAdminOrOwner(tenantId, currentUserEmail)) {
            throw new InsufficientPermissionException("Chỉ ADMIN hoặc OWNER mới có quyền xóa tenant");
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

    @Transactional(readOnly = true)
    public TenantResponse switchTenant(Long tenantId) {
        return getTenantForCurrentUser(tenantId);
    }

    @Transactional(readOnly = true)
    public TenantResponse switchTenantByKey(String tenantKey) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found with key: " + tenantKey));
        return getTenantForCurrentUser(tenant.getId());
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    @Transactional(rollbackFor = Exception.class)
    public TenantResponse updateBasicInfo(String tenantKey, TenantBasicInfoRequest req) {
        if (tenantKey == null || tenantKey.trim().isEmpty()) {
            throw new InvalidTenantKeyException("Tenant key không được để trống");
        }

        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new TenantNotFoundException("Tenant không tồn tại với key: " + tenantKey));

        String currentUserEmail = getCurrentUserEmail();
        if (!permissionValidator.isAdminOrOwner(tenant.getId(), currentUserEmail)) {
            throw new InsufficientPermissionException("Bạn không có quyền cập nhật thông tin tenant này");
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
        if (req.getExpiresAt() != null) {
            tenant.setExpiresAt(req.getExpiresAt());
        }

        tenantRepository.save(tenant);
        auditLogService.logAction(tenant.getId(), currentUserEmail, "UPDATE_BASIC_INFO",
                "Updated basic info for tenant " + tenantKey);
        return TenantMapper.toResponse(tenant);
    }

    /**
     * Cập nhật thông tin liên hệ. Dùng typed DTO thay vì Map<String,Object>.
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantResponse updateContactInfo(String tenantKey, TenantContactInfoRequest req) {
        if (tenantKey == null || tenantKey.trim().isEmpty()) {
            throw new InvalidTenantKeyException("Tenant key không được để trống");
        }

        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
                .orElseThrow(() -> new TenantNotFoundException("Tenant không tồn tại với key: " + tenantKey));

        String currentUserEmail = getCurrentUserEmail();
        if (!permissionValidator.isAdminOrOwner(tenant.getId(), currentUserEmail)) {
            throw new InsufficientPermissionException("Bạn không có quyền cập nhật thông tin liên hệ tenant này");
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

    @Transactional
    public List<InvitationResponse> bulkInviteUsers(String tenantKey, List<BulkInvitationRequest.Invitation> invitations) {
        String currentUserEmail = getCurrentUserEmail();

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

    private void createEmptyTenantProfile(Tenant tenant) {
        com.chatbot.core.tenant.profile.dto.TenantProfileRequest profileRequest =
                new com.chatbot.core.tenant.profile.dto.TenantProfileRequest();
        profileRequest.setDescription("");
        profileRequest.setIndustry("");
        profileRequest.setPlan("");
        profileRequest.setCompanySize("");
        profileRequest.setLegalName(tenant.getName());
        profileRequest.setTaxCode("");
        profileRequest.setContactEmail("");
        profileRequest.setContactPhone("");
        profileRequest.setLogoUrl("");
        profileRequest.setFaviconUrl("");
        profileRequest.setPrimaryColor("");
        tenantProfileService.upsertProfile(tenant.getId(), profileRequest);
        log.info("[TenantService] Created empty profile for tenant: {}", tenant.getId());
    }

    private void createEmptyAddressForTenant(Long tenantId) {
        com.chatbot.shared.address.dto.AddressRequestDTO emptyAddress =
                new com.chatbot.shared.address.dto.AddressRequestDTO();
        emptyAddress.setOwnerType(OwnerType.TENANT);
        emptyAddress.setOwnerId(tenantId);
        emptyAddress.setStreet("");
        emptyAddress.setHouseNumber("");
        emptyAddress.setWard("");
        emptyAddress.setDistrict("");
        emptyAddress.setProvince("");
        emptyAddress.setCountry("Vietnam");
        addressService.createAddress(tenantId, emptyAddress);
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
