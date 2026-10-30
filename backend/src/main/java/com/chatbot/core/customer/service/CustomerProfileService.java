package com.chatbot.core.customer.service;

import com.chatbot.core.customer.dto.*;
import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerProfileService {

    private final CustomerProfileRepository customerProfileRepository;

    public CustomerProfileDTO createCustomerProfile(CreateCustomerProfileRequest request, Long tenantId) {
        log.info("Creating customer profile for externalId: {}, tenantId: {}", request.getExternalId(), tenantId);

        if (customerProfileRepository.existsByExternalIdAndTenantId(request.getExternalId(), tenantId)) {
            throw new IllegalArgumentException("Customer profile with externalId " + request.getExternalId() + " already exists");
        }

        CustomerProfile profile = CustomerProfile.builder()
                .externalId(request.getExternalId())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .avatarUrl(request.getAvatarUrl())
                .status(request.getStatus() != null ? CustomerProfile.CustomerStatus.valueOf(request.getStatus()) : CustomerProfile.CustomerStatus.ACTIVE)
                .tier(request.getTier() != null ? CustomerProfile.CustomerTier.valueOf(request.getTier()) : CustomerProfile.CustomerTier.FREE)
                .firstContactAt(LocalDateTime.now())
                .lastContactAt(LocalDateTime.now())
                .totalConversations(0)
                .totalMessages(0)
                .customAttributes(request.getCustomAttributes())
                .tags(request.getTags())
                .preferredLanguage(request.getPreferredLanguage())
                .timezone(request.getTimezone())
                .isBlocked(false)
                .build();

        profile.setTenantId(tenantId);

        CustomerProfile saved = customerProfileRepository.save(profile);
        log.info("Customer profile created with id: {}", saved.getId());
        return toDTO(saved);
    }

    public CustomerProfileDTO getCustomerProfileById(Long id, Long tenantId) {
        CustomerProfile profile = customerProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer profile not found with id: " + id));

        if (!profile.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Access denied: Customer profile does not belong to tenant");
        }

        return toDTO(profile);
    }

    public CustomerProfileDTO getCustomerProfileByExternalId(String externalId, Long tenantId) {
        CustomerProfile profile = customerProfileRepository.findByExternalIdAndTenantId(externalId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Customer profile not found with externalId: " + externalId));
        return toDTO(profile);
    }

    public List<CustomerProfileDTO> getAllCustomerProfiles(Long tenantId) {
        List<CustomerProfile> profiles = customerProfileRepository.findByTenantId(tenantId);
        return profiles.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CustomerProfileDTO> getCustomerProfilesByStatus(Long tenantId, String status) {
        List<CustomerProfile> profiles = customerProfileRepository.findByTenantIdAndStatus(
                tenantId, CustomerProfile.CustomerStatus.valueOf(status));
        return profiles.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CustomerProfileDTO> getCustomerProfilesByTier(Long tenantId, String tier) {
        List<CustomerProfile> profiles = customerProfileRepository.findByTenantIdAndTier(
                tenantId, CustomerProfile.CustomerTier.valueOf(tier));
        return profiles.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerProfileDTO updateCustomerProfile(Long id, UpdateCustomerProfileRequest request, Long tenantId) {
        CustomerProfile profile = customerProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer profile not found with id: " + id));

        if (!profile.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Access denied: Customer profile does not belong to tenant");
        }

        if (request.getName() != null) profile.setName(request.getName());
        if (request.getEmail() != null) profile.setEmail(request.getEmail());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getStatus() != null) profile.setStatus(CustomerProfile.CustomerStatus.valueOf(request.getStatus()));
        if (request.getTier() != null) profile.setTier(CustomerProfile.CustomerTier.valueOf(request.getTier()));
        if (request.getPreferredLanguage() != null) profile.setPreferredLanguage(request.getPreferredLanguage());
        if (request.getTimezone() != null) profile.setTimezone(request.getTimezone());
        if (request.getCustomAttributes() != null) profile.setCustomAttributes(request.getCustomAttributes());
        if (request.getTags() != null) profile.setTags(request.getTags());
        if (request.getIsBlocked() != null) {
            profile.setIsBlocked(request.getIsBlocked());
            if (request.getIsBlocked()) {
                profile.setBlockedReason(request.getBlockedReason());
                profile.setBlockedAt(LocalDateTime.now());
                profile.setStatus(CustomerProfile.CustomerStatus.BLOCKED);
            } else {
                profile.setBlockedReason(null);
                profile.setBlockedAt(null);
                profile.setStatus(CustomerProfile.CustomerStatus.ACTIVE);
            }
        }

        CustomerProfile saved = customerProfileRepository.save(profile);
        log.info("Customer profile updated with id: {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public void deleteCustomerProfile(Long id, Long tenantId) {
        CustomerProfile profile = customerProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer profile not found with id: " + id));

        if (!profile.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Access denied: Customer profile does not belong to tenant");
        }

        customerProfileRepository.delete(profile);
        log.info("Customer profile deleted with id: {}", id);
    }

    @Transactional
    public void updateConversationStats(Long customerProfileId, Long tenantId, Integer messageCount, Integer responseTimeSeconds) {
        CustomerProfile profile = customerProfileRepository.findById(customerProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Customer profile not found with id: " + customerProfileId));

        if (!profile.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Access denied: Customer profile does not belong to tenant");
        }

        profile.setLastContactAt(LocalDateTime.now());
        profile.setTotalConversations(profile.getTotalConversations() + 1);
        profile.setTotalMessages(profile.getTotalMessages() + (messageCount != null ? messageCount : 0));

        if (responseTimeSeconds != null) {
            int currentAvg = profile.getAvgResponseTimeSeconds() != null ? profile.getAvgResponseTimeSeconds() : 0;
            int totalConv = profile.getTotalConversations();
            profile.setAvgResponseTimeSeconds((currentAvg * (totalConv - 1) + responseTimeSeconds) / totalConv);
        }

        customerProfileRepository.save(profile);
    }

    private CustomerProfileDTO toDTO(CustomerProfile profile) {
        CustomerProfileDTO dto = new CustomerProfileDTO();
        dto.setId(profile.getId());
        dto.setExternalId(profile.getExternalId());
        dto.setName(profile.getName());
        dto.setEmail(profile.getEmail());
        dto.setPhone(profile.getPhone());
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setStatus(profile.getStatus() != null ? profile.getStatus().name() : null);
        dto.setTier(profile.getTier() != null ? profile.getTier().name() : null);
        dto.setFirstContactAt(profile.getFirstContactAt());
        dto.setLastContactAt(profile.getLastContactAt());
        dto.setTotalConversations(profile.getTotalConversations());
        dto.setTotalMessages(profile.getTotalMessages());
        dto.setAvgResponseTimeSeconds(profile.getAvgResponseTimeSeconds());
        dto.setSatisfactionScore(profile.getSatisfactionScore());
        dto.setCustomAttributes(profile.getCustomAttributes());
        dto.setTags(profile.getTags());
        dto.setPreferredLanguage(profile.getPreferredLanguage());
        dto.setTimezone(profile.getTimezone());
        dto.setIsBlocked(profile.getIsBlocked());
        dto.setBlockedReason(profile.getBlockedReason());
        dto.setBlockedAt(profile.getBlockedAt());
        return dto;
    }
}
