package com.chatbot.core.tenant.service;

import com.chatbot.modules.address.model.OwnerType;
import com.chatbot.modules.address.service.AddressService;

import java.util.stream.Collectors;
import java.util.Map;
import com.chatbot.core.identity.model.Auth;
import com.chatbot.core.identity.repository.AuthRepository;
import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.identity.model.SystemRole;
import com.chatbot.core.tenant.dto.*;
import com.chatbot.core.tenant.mapper.TenantMapper;
import com.chatbot.core.tenant.model.*;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.membership.model.TenantMember;
import com.chatbot.core.tenant.membership.model.TenantRole;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.profile.model.TenantProfile;
import com.chatbot.core.tenant.profile.repository.TenantProfileRepository;
import com.chatbot.core.tenant.profile.service.TenantProfileService;

import com.chatbot.modules.address.dto.AddressDetailResponseDTO;
import com.chatbot.core.tenant.profile.dto.TenantProfileResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMemberRepository tenantMemberRepository;
    private final AuthRepository authRepository;
    private final TenantProfileService tenantProfileService;
    private final TenantProfileRepository tenantProfileRepository;
    private final AddressService addressService;

    /**
     * Tạo tenant mới và gán user hiện tại làm OWNER.
     */
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        log.info("🏗️ [TenantService] Starting tenant creation process");
        
        String currentUserEmail =
                SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("👤 [TenantService] Current user: {}", currentUserEmail);

        Auth currentUser = authRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        log.info("✅ [TenantService] User found: id={}, email={}", currentUser.getId(), currentUser.getEmail());

        // Set default visibility to PUBLIC if not provided
        if (request.getVisibility() == null) {
            request.setVisibility(TenantVisibility.PUBLIC);
            log.info("⚙️ [TenantService] Set default visibility to PUBLIC");
        }

        log.info("📋 [TenantService] Creating tenant with name={}, visibility={}", request.getName(), request.getVisibility());
        
        Tenant tenant = TenantMapper.toEntity(request);
        log.info("🏢 [TenantService] Tenant entity created: tenantKey={}", tenant.getTenantKey());
        
        // Save the tenant first to generate the ID
        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("💾 [TenantService] Tenant saved to database: id={}, tenantKey={}", savedTenant.getId(), savedTenant.getTenantKey());

        // Create tenant member
        log.info("👥 [TenantService] Creating tenant membership for user {} as OWNER", currentUser.getId());
        TenantMember owner = TenantMember.builder()
                .tenant(savedTenant)
                .user(currentUser)
                .role(TenantRole.OWNER)
                .status(MembershipStatus.ACTIVE)
                .build();
        tenantMemberRepository.save(owner);
        log.info("✅ [TenantService] Tenant membership created successfully");

        // Create empty tenant profile
        try {
            log.info("📄 [TenantService] Creating tenant profile for tenant {}", savedTenant.getId());
            createEmptyTenantProfile(savedTenant);
            log.info("✅ [TenantService] Tenant profile created successfully");
        } catch (Exception e) {
            log.error("❌ [TenantService] Failed to create tenant profile for tenant {}: {}", savedTenant.getId(), e.getMessage(), e);
        }

        // Create empty address
        try {
            log.info("🏠 [TenantService] Creating address for tenant {}", savedTenant.getId());
            createEmptyAddressForTenant(savedTenant.getId());
            log.info("✅ [TenantService] Address created successfully");
        } catch (Exception e) {
            log.error("❌ [TenantService] Failed to create address for tenant {}: {}", savedTenant.getId(), e.getMessage(), e);
        }

        TenantResponse response = TenantMapper.toResponse(savedTenant);
        log.info("🎉 [TenantService] Tenant creation completed successfully: tenantKey={}, name={}", 
                response.getTenantKey(), response.getName());
        
        return response;
    }

    /**
     * Lấy tenant theo ID (internal use).
     */
    @Transactional(readOnly = true)
    public Tenant getTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
    }

    /**
     * Suspend tenant.
     */
    @Transactional
    public void suspendTenant(Long tenantId) {
        // Lấy thông tin tenant
        Tenant tenant = getTenant(tenantId);
        
        // Kiểm tra quyền admin
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = authRepository.findByEmail(currentUserEmail)
                .map(auth -> auth.getSystemRole() == SystemRole.ADMIN)
                .orElse(false);
                
        if (!isAdmin) {
            throw new RuntimeException("Chỉ admin mới có quyền tạm dừng tenant");
        }
        
        // Kiểm tra trạng thái hiện tại
        if (tenant.getStatus() == TenantStatus.SUSPENDED) {
            throw new RuntimeException("Tenant đã ở trạng thái tạm dừng");
        }
        
        // Thực hiện cập nhật
        tenant.setStatus(TenantStatus.SUSPENDED);
        tenant.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Deactivate tenant.
     */
    @Transactional
    public void deactivateTenant(Long tenantId) {
        // Lấy thông tin tenant
        Tenant tenant = getTenant(tenantId);
        
        // Kiểm tra quyền owner
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isOwner = tenantMemberRepository.existsByTenantIdAndUserEmailAndRoleAndStatus(
                tenantId, 
                currentUserEmail, 
                TenantRole.OWNER,
                MembershipStatus.ACTIVE
        );
        
        if (!isOwner) {
            throw new RuntimeException("Chỉ chủ sở hữu mới có quyền vô hiệu hóa tenant");
        }
        
        // Kiểm tra trạng thái hiện tại
        if (tenant.getStatus() == TenantStatus.INACTIVE) {
            throw new RuntimeException("Tenant đã ở trạng thái không hoạt động");
        }
        
        // Thực hiện cập nhật
        tenant.setStatus(TenantStatus.INACTIVE);
        tenant.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Activate tenant.
     */
    @Transactional
    public void activateTenant(Long tenantId) {
        // Lấy thông tin tenant
        Tenant tenant = getTenant(tenantId);
        
        // Kiểm tra quyền admin
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isAdmin = authRepository.findByEmail(currentUserEmail)
                .map(auth -> auth.getSystemRole() == SystemRole.ADMIN)
                .orElse(false);
                
        if (!isAdmin) {
            throw new RuntimeException("Chỉ admin mới có quyền kích hoạt tenant");
        }
        
        // Kiểm tra trạng thái hiện tại
        if (tenant.getStatus() == TenantStatus.ACTIVE) {
            throw new RuntimeException("Tenant đã ở trạng thái hoạt động");
        }
        
        // Thực hiện cập nhật
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Lấy danh sách tenant mà user hiện tại là member.
     */
    @Transactional(readOnly = true)
    public List<TenantDetailResponse> getUserTenantsDetail() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. Lấy danh sách Tenant mà user là member
        List<Tenant> tenants = tenantMemberRepository
                .findByUserEmailWithTenant(currentUserEmail)
                .stream()
                .map(TenantMember::getTenant)
                .collect(Collectors.toList());

        if (tenants.isEmpty()) return Collections.emptyList();

        List<Long> tenantIds = tenants.stream().map(Tenant::getId).collect(Collectors.toList());

        // 2. Lấy Map Profile theo danh sách ID (để tránh loop query)
        // Giả sử bạn có repository hỗ trợ lấy theo danh sách ID
        Map<Long, TenantProfileResponse> profiles = tenantProfileService.getProfilesByTenantIds(tenantIds);
        // Addresses không còn theo tenant, bỏ qua

        // 3. Map sang TenantDetailResponse
        return tenants.stream().map(tenant -> {
            TenantProfileResponse profile = profiles.get(tenant.getId());
            List<AddressDetailResponseDTO> addrList = Collections.emptyList(); // Không còn address theo tenant
            
            // Sử dụng hàm từ bạn đã viết trong DTO
            return TenantDetailResponse.from(
                TenantMapper.toResponse(tenant), 
                profile, 
                addrList
            );
        }).collect(Collectors.toList());
    }

    /**
     * Lấy thông tin tenant nếu user hiện tại là member.
     */
    @Transactional(readOnly = true)
    public TenantResponse getTenantForCurrentUser(Long tenantId) {
        String currentUserEmail =
                SecurityContextHolder.getContext().getAuthentication().getName();

        // Chỉ cho phép truy cập nếu thành viên có trạng thái ACTIVE
        TenantMember member =
                tenantMemberRepository
                        .findByTenantIdAndUserEmailAndStatus(
                                tenantId, 
                                currentUserEmail,
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new RuntimeException("Bạn không có quyền truy cập tenant này"));

        return TenantMapper.toResponse(member.getTenant());
    }
    
    /**
     * Lấy thông tin chi tiết tenant bao gồm profile và địa chỉ
     * @param tenantId ID của tenant cần lấy thông tin
     * @return Đối tượng TenantDetailResponse chứa đầy đủ thông tin
     */
    @Transactional(readOnly = true)
    public TenantDetailResponse getTenantDetail(Long tenantId) {
        // Kiểm tra quyền truy cập
        TenantResponse tenantResponse = getTenantForCurrentUser(tenantId);
        
        // Lấy thông tin profile
        TenantProfileResponse profile = tenantProfileService.getProfile(tenantId);
        
        // Lấy địa chỉ duy nhất của tenant
        AddressDetailResponseDTO addressDetail = null;
        try {
            addressDetail = addressService.getSingleAddressByOwner(tenantId, OwnerType.TENANT, tenantId);
        } catch (RuntimeException e) {
            // Tenant chưa có địa chỉ, bỏ qua
        }
        
        // Tạo và trả về response
        TenantDetailResponse response = TenantDetailResponse.from(tenantResponse, profile, addressDetail);
        response.setAddress(addressDetail);
        return response;
    }

    /**
     * Lấy thông tin chi tiết tenant bằng tenantKey (cho frontend)
     * @param tenantKey UUID của tenant cần lấy thông tin
     * @return Đối tượng TenantDetailResponse chứa đầy đủ thông tin
     */
    @Transactional(readOnly = true)
    public TenantDetailResponse getTenantDetailByTenantKey(String tenantKey) {
        // Tìm tenant bằng tenantKey
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
            .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
        
        // Map tenant sang TenantResponse
        TenantResponse tenantResponse = TenantMapper.toResponse(tenant);
        
        // Lấy thông tin profile
        TenantProfileResponse profile = tenantProfileService.getProfile(tenant.getId());
        
        // Lấy địa chỉ duy nhất của tenant
        AddressDetailResponseDTO addressDetail = null;
        try {
            addressDetail = addressService.getSingleAddressByOwner(tenant.getId(), OwnerType.TENANT, tenant.getId());
        } catch (RuntimeException e) {
            // Tenant chưa có địa chỉ, bỏ qua
        }
        
        // Tạo và trả về response
        TenantDetailResponse response = TenantDetailResponse.from(tenantResponse, profile, addressDetail);
        response.setAddress(addressDetail);
        return response;
    }

    /**
     * Switch tenant (validate membership).
     */
    @Transactional
    public TenantResponse switchTenant(Long tenantId) {
        return getTenantForCurrentUser(tenantId);
    }

    /**
     * Switch tenant bằng tenantKey (validate membership).
     */
    @Transactional
    public TenantResponse switchTenantByKey(String tenantKey) {
        // Tìm tenant bằng tenantKey
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
            .orElseThrow(() -> new RuntimeException("Tenant not found with key: " + tenantKey));
        
        return getTenantForCurrentUser(tenant.getId());
    }

    /**
     * Search tenant theo keyword + paging.
     * Trả thêm cờ isMember cho FE.
     */
        @Transactional(readOnly = true)
        public Page<TenantSearchResponse> searchTenants(
                TenantSearchRequest request,
                String currentUserEmail) {

        // 1. Tìm kiếm danh sách Tenant theo phân trang
        Page<Tenant> tenantsPage = tenantRepository.findByVisibilityAndStatusAndNameContainingIgnoreCase(
                TenantVisibility.PUBLIC, 
                TenantStatus.ACTIVE, 
                request.getKeyword() != null ? request.getKeyword() : "",
                request.toPageable()
        );

        // 2. Lấy danh sách tất cả quan hệ hội viên của User này
        List<TenantMember> userMemberships = tenantMemberRepository.findByUserEmail(currentUserEmail);

        // 3. Lấy thông tin profile của tất cả tenant trong kết quả tìm kiếm
        List<Long> tenantIds = tenantsPage.getContent().stream()
                .map(Tenant::getId)
                .collect(Collectors.toList());
        Map<Long, com.chatbot.core.tenant.profile.dto.TenantProfileResponse> profilesMap = 
            tenantProfileService.getProfilesByTenantIds(tenantIds);

        // 4. Map sang Response
        return tenantsPage.map(tenant -> {
            // Tìm xem tenant hiện tại có nằm trong danh sách hội viên của user không
            TenantMembershipStatus status = userMemberships.stream()
                    .filter(m -> m.getTenant().getId().equals(tenant.getId()))
                    .findFirst()
                    .map(m -> {
                        // Map từ MembershipStatus (của Entity) sang TenantMembershipStatus (của DTO)
                        if (m.getStatus() == MembershipStatus.PENDING) return TenantMembershipStatus.PENDING;
                        if (m.getStatus() == MembershipStatus.ACTIVE) return TenantMembershipStatus.APPROVED;
                        return TenantMembershipStatus.NONE;
                    })
                    .orElse(TenantMembershipStatus.NONE);

            // Lấy thông tin profile nếu có
            com.chatbot.core.tenant.profile.dto.TenantProfileResponse profile = 
                profilesMap.get(tenant.getId());

            return TenantSearchResponse.builder()
                    .id(tenant.getId())
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
    
    /**
     * Tạo profile rỗng cho tenant mới
     */
    private void createEmptyTenantProfile(Tenant tenant) {
        // Create a new tenant profile with default values
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
        
        // Save the profile using upsertProfile
        tenantProfileService.upsertProfile(tenant.getId(), profileRequest);
        log.info("Created empty tenant profile for tenant: {}", tenant.getId());
    }
    
    /**
     * Tạo địa chỉ rỗng cho tenant mới
     */
    private void createEmptyAddressForTenant(Long tenantId) {
        // Create an empty address DTO
        com.chatbot.modules.address.dto.AddressRequestDTO emptyAddress = 
            new com.chatbot.modules.address.dto.AddressRequestDTO();
            
        emptyAddress.setOwnerType(OwnerType.TENANT);
        emptyAddress.setOwnerId(tenantId);
        emptyAddress.setStreet("");
        emptyAddress.setHouseNumber("");
        emptyAddress.setWard("");
        emptyAddress.setDistrict("");
        emptyAddress.setProvince("");
        emptyAddress.setCountry("Vietnam");  // Add this line
        
        // Save the address
        addressService.createAddress(tenantId, emptyAddress);
        log.info("Created empty address for tenant: {}", tenantId);
    }

    @Transactional
    public TenantResponse updateBasicInfo(String tenantKey, TenantBasicInfoRequest req) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant không tồn tại với key: " + tenantKey));

        // Chỉ cập nhật các trường không null
        if (req.getName() != null) {
            tenant.setName(req.getName());
        }
        if (req.getStatus() != null) {
            tenant.setStatus(req.getStatus());
        }
        if (req.getVisibility() != null) {
            tenant.setVisibility(req.getVisibility());
        }
        if (req.getExpiresAt() != null) {
            tenant.setExpiresAt(req.getExpiresAt());
        }

        tenantRepository.save(tenant);
        return TenantMapper.toResponse(tenant);
    }

    @Transactional
    public TenantResponse updateContactInfo(String tenantKey, Map<String, Object> contactData) {
        Tenant tenant = tenantRepository.findByTenantKey(tenantKey)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant không tồn tại với key: " + tenantKey));

        // Update contact fields in tenant profile
        TenantProfile profile = tenantProfileRepository.findByTenant_Id(tenant.getId())
                .orElseGet(() -> {
                    TenantProfile p = new TenantProfile();
                    p.setTenant(tenant);
                    return p;
                });

        if (contactData.containsKey("email")) {
            profile.setContactEmail((String) contactData.get("email"));
        }
        if (contactData.containsKey("phone")) {
            profile.setContactPhone((String) contactData.get("phone"));
        }
        if (contactData.containsKey("website")) {
            // Store website in tenant or profile - decide based on your data model
            // For now, let's store it in tenant as a custom field or ignore
        }

        tenantProfileRepository.save(profile);
        
        // Return updated tenant with contact info
        return TenantMapper.toResponseWithProfile(tenant, profile);
    }
}
