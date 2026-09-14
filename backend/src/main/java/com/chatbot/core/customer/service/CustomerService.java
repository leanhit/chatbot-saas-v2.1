package com.chatbot.core.customer.service;

import com.chatbot.core.customer.dto.CustomerDto;
import com.chatbot.core.customer.dto.CustomerStatsDto;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.spokes.facebook.user.model.FacebookUser;
import com.chatbot.spokes.facebook.user.repository.FacebookUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final FacebookUserRepository facebookUserRepository;

    @Transactional(readOnly = true)
    public Page<CustomerDto> getCustomers(Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.warn("No tenant ID found in context when fetching customers");
            return Page.empty(pageable);
        }

        Page<FacebookUser> usersPage = facebookUserRepository.findByTenantId(tenantId, pageable);
        return usersPage.map(this::mapToCustomerDto);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerByPsid(String psid) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("No tenant ID found in context");
        }

        FacebookUser user = facebookUserRepository.findByPsidAndTenantId(psid, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with PSID: " + psid));

        return mapToCustomerDto(user);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDto> searchCustomers(String keyword, Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return Page.empty(pageable);
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return getCustomers(pageable);
        }

        Page<FacebookUser> usersPage = facebookUserRepository.searchByTenantIdAndNameContaining(tenantId, keyword.trim(), pageable);
        return usersPage.map(this::mapToCustomerDto);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDto> getCustomersByStatus(String status, Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return Page.empty(pageable);
        }

        if ("PENDING".equalsIgnoreCase(status)) {
            Page<FacebookUser> unmapped = facebookUserRepository.findByPageIdAndTenantIdAndOdooPartnerIdIsNull(null, tenantId, pageable);
            return unmapped.map(this::mapToCustomerDto);
        }

        // Default list for other statuses
        return getCustomers(pageable);
    }

    @Transactional(readOnly = true)
    public CustomerStatsDto getCustomerStats() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return CustomerStatsDto.builder()
                    .totalCustomers(0)
                    .pendingCustomers(0)
                    .completedCustomers(0)
                    .totalCapturedPhones(0)
                    .build();
        }

        long total = facebookUserRepository.countByTenantId(tenantId);
        long completed = 0; // Odoo mapped users or completed processing
        long pending = total - completed;
        long capturedPhones = 0;

        return CustomerStatsDto.builder()
                .totalCustomers(total)
                .pendingCustomers(pending)
                .completedCustomers(completed)
                .totalCapturedPhones(capturedPhones)
                .build();
    }

    public List<String> getAvailableStatuses() {
        return List.of("PENDING", "COMPLETED", "FAILED");
    }

    private CustomerDto mapToCustomerDto(FacebookUser user) {
        String status = user.getOdooPartnerId() != null ? "COMPLETED" : "PENDING";
        LocalDateTime updateTime = user.getUpdatedAt() != null ? user.getUpdatedAt() :
                (user.getLastInteraction() != null ? user.getLastInteraction() : user.getCreatedAt());

        return CustomerDto.builder()
                .psid(user.getPsid())
                .displayName(user.getName() != null ? user.getName() : "Customer " + user.getPsid().substring(0, Math.min(6, user.getPsid().length())))
                .displayAvatar(user.getProfilePic())
                .primaryPhone(null)
                .email(null)
                .totalPhones(0)
                .status(status)
                .updatedAt(updateTime)
                .build();
    }
}
