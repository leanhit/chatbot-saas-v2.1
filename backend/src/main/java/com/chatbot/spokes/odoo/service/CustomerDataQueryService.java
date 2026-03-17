package com.chatbot.spokes.odoo.service;

import com.chatbot.spokes.odoo.dto.CustomerDataDTO;
import com.chatbot.spokes.odoo.model.FbCustomerStaging;
import com.chatbot.spokes.odoo.model.FbCapturedPhone;
import com.chatbot.spokes.facebook.user.model.FacebookUser;
import com.chatbot.spokes.facebook.user.repository.FacebookUserRepository;
import com.chatbot.spokes.odoo.repository.FbCustomerStagingRepository;
import com.chatbot.spokes.odoo.repository.FbCapturedPhoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service để query và gộp data từ 3 bảng customer
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerDataQueryService {

    private final FbCustomerStagingRepository stagingRepository;
    private final FacebookUserRepository facebookUserRepository;
    private final FbCapturedPhoneRepository capturedPhoneRepository;
    private final CustomerDataServiceMapper mapper;

    /**
     * Lấy tất cả customer data với pagination
     */
    public Page<CustomerDataDTO> getAllCustomers(Pageable pageable) {
        Long tenantId = getCurrentTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Tenant ID not found in context");
        }

        // 1. Lấy staging data
        Page<FbCustomerStaging> stagingPage = stagingRepository.findByTenantIdOrderByUpdatedAtDesc(tenantId, pageable);
        
        // 2. Lấy Facebook users cho các PSID
        Set<String> psids = stagingPage.getContent().stream()
                .map(FbCustomerStaging::getPsid)
                .collect(Collectors.toSet());
        Map<String, FacebookUser> facebookUserMap = facebookUserRepository
                .findByPsidInAndTenantId(psids, tenantId)
                .stream()
                .collect(Collectors.toMap(FacebookUser::getPsid, user -> user));

        // 3. Lấy captured phones cho các owners
        Set<String> ownerIds = stagingPage.getContent().stream()
                .map(FbCustomerStaging::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, List<FbCapturedPhone>> capturedPhoneMap = new HashMap<>();
        if (!ownerIds.isEmpty()) {
            capturedPhoneMap = capturedPhoneRepository.findByOwnerIdInAndTenantId(ownerIds, tenantId)
                    .stream()
                    .collect(Collectors.groupingBy(FbCapturedPhone::getOwnerId));
        }

        // 4. Map sang DTO
        List<CustomerDataDTO> customerDataList = mapper.mapToCustomerDataList(
                stagingPage.getContent(),
                facebookUserMap,
                capturedPhoneMap
        );

        return new org.springframework.data.domain.PageImpl<>(
                customerDataList,
                stagingPage.getPageable(),
                stagingPage.getTotalElements()
        );
    }

    /**
     * Lấy customer data theo PSID
     */
    public CustomerDataDTO getCustomerByPsid(String psid) {
        Long tenantId = getCurrentTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Tenant ID not found in context");
        }

        // 1. Lấy staging data
        Optional<FbCustomerStaging> stagingOpt = stagingRepository.findByPsidAndTenantId(psid, tenantId);
        if (stagingOpt.isEmpty()) {
            return null;
        }

        // 2. Lấy Facebook user
        Optional<FacebookUser> facebookUserOpt = facebookUserRepository.findByPsidAndTenantId(psid, tenantId);

        // 3. Lấy captured phones
        FbCustomerStaging staging = stagingOpt.get();
        List<FbCapturedPhone> capturedPhones = new ArrayList<>();
        if (staging.getOwnerId() != null) {
            capturedPhones = capturedPhoneRepository.findByOwnerIdAndTenantId(staging.getOwnerId(), tenantId);
        }

        return mapper.mapToCustomerData(staging, facebookUserOpt.orElse(null), capturedPhones);
    }

    /**
     * Tìm kiếm customer data theo tên hoặc SĐT
     */
    public Page<CustomerDataDTO> searchCustomers(String keyword, Pageable pageable) {
        Long tenantId = getCurrentTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Tenant ID not found in context");
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers(pageable);
        }

        // 2. Tìm trong Facebook users theo tên
        List<FacebookUser> facebookUsers = facebookUserRepository
                .findByNameContainingIgnoreCaseAndTenantId(keyword, tenantId);
        Set<String> psidsFromName = facebookUsers.stream()
                .map(FacebookUser::getPsid)
                .collect(Collectors.toSet());

        // 3. Tìm trong staging data theo phone
        List<FbCustomerStaging> stagingByPhone = stagingRepository
                .findByPhonesContainingAndTenantId(keyword, tenantId);
        Set<String> psidsFromPhone = stagingByPhone.stream()
                .map(FbCustomerStaging::getPsid)
                .collect(Collectors.toSet());

        // 4. Gộp tất cả PSID
        Set<String> allPsids = new HashSet<>(psidsFromName);
        allPsids.addAll(psidsFromPhone);

        if (allPsids.isEmpty()) {
            return Page.empty(pageable);
        }

        // 5. Lấy staging data cho các PSID tìm được
        List<FbCustomerStaging> stagingList = stagingRepository.findByPsidInAndTenantId(allPsids, tenantId);

        // 6. Map sang DTO (tương tự như getAllCustomers)
        Map<String, FacebookUser> facebookUserMap = facebookUsers.stream()
                .collect(Collectors.toMap(FacebookUser::getPsid, user -> user));

        Set<String> ownerIds = stagingList.stream()
                .map(FbCustomerStaging::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, List<FbCapturedPhone>> capturedPhoneMap = new HashMap<>();
        if (!ownerIds.isEmpty()) {
            capturedPhoneMap = capturedPhoneRepository.findByOwnerIdInAndTenantId(ownerIds, tenantId)
                    .stream()
                    .collect(Collectors.groupingBy(FbCapturedPhone::getOwnerId));
        }

        List<CustomerDataDTO> customerDataList = mapper.mapToCustomerDataList(
                stagingList,
                facebookUserMap,
                capturedPhoneMap
        );

        return new org.springframework.data.domain.PageImpl<>(
                customerDataList,
                pageable,
                customerDataList.size()
        );
    }

    /**
     * Lấy customer data theo status
     */
    public Page<CustomerDataDTO> getCustomersByStatus(com.chatbot.spokes.odoo.model.CustomerStatus status, Pageable pageable) {
        Long tenantId = getCurrentTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Tenant ID not found in context");
        }

        Page<FbCustomerStaging> stagingPage = stagingRepository.findByTenantIdAndStatusOrderByUpdatedAtDesc(tenantId, status, pageable);
        
        // Map tương tự như getAllCustomers
        Set<String> psids = stagingPage.getContent().stream()
                .map(FbCustomerStaging::getPsid)
                .collect(Collectors.toSet());
        Map<String, FacebookUser> facebookUserMap = facebookUserRepository
                .findByPsidInAndTenantId(psids, tenantId)
                .stream()
                .collect(Collectors.toMap(FacebookUser::getPsid, user -> user));

        Set<String> ownerIds = stagingPage.getContent().stream()
                .map(FbCustomerStaging::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, List<FbCapturedPhone>> capturedPhoneMap = new HashMap<>();
        if (!ownerIds.isEmpty()) {
            capturedPhoneMap = capturedPhoneRepository.findByOwnerIdInAndTenantId(ownerIds, tenantId)
                    .stream()
                    .collect(Collectors.groupingBy(FbCapturedPhone::getOwnerId));
        }

        List<CustomerDataDTO> customerDataList = mapper.mapToCustomerDataList(
                stagingPage.getContent(),
                facebookUserMap,
                capturedPhoneMap
        );

        return new org.springframework.data.domain.PageImpl<>(
                customerDataList,
                stagingPage.getPageable(),
                stagingPage.getTotalElements()
        );
    }

    /**
     * Lấy thống kê customer data
     */
    public Map<String, Object> getCustomerStats() {
        Long tenantId = getCurrentTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Tenant ID not found in context");
        }

        long totalCustomers = stagingRepository.countByTenantId(tenantId);
        long pendingCustomers = stagingRepository.countByTenantIdAndStatus(tenantId, com.chatbot.spokes.odoo.model.CustomerStatus.PENDING);
        long completedCustomers = stagingRepository.countByTenantIdAndStatus(tenantId, com.chatbot.spokes.odoo.model.CustomerStatus.COMPLETED);
        long syncedCustomers = stagingRepository.countByTenantIdAndStatus(tenantId, com.chatbot.spokes.odoo.model.CustomerStatus.PUSHED_TO_ODOO);
        long totalCapturedPhones = capturedPhoneRepository.countByTenantId(tenantId);
        long totalFacebookUsers = facebookUserRepository.countByTenantId(tenantId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", totalCustomers);
        stats.put("pendingCustomers", pendingCustomers);
        stats.put("completedCustomers", completedCustomers);
        stats.put("syncedCustomers", syncedCustomers);
        stats.put("totalCapturedPhones", totalCapturedPhones);
        stats.put("totalFacebookUsers", totalFacebookUsers);

        return stats;
    }

    private Long getCurrentTenantId() {
        return com.chatbot.core.tenant.infra.TenantContext.getTenantId();
    }
}
