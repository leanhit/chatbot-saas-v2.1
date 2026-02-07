package com.chatbot.modules.tenant.core.service;

import com.chatbot.modules.tenant.core.dto.CreateTenantRequest;
import com.chatbot.modules.tenant.core.dto.TenantResponse;
import com.chatbot.modules.tenant.core.dto.TenantSearchRequest;
import com.chatbot.modules.tenant.core.model.Tenant;
import com.chatbot.modules.tenant.core.model.TenantStatus;
import com.chatbot.modules.tenant.core.model.TenantVisibility;
import com.chatbot.modules.tenant.core.repository.TenantRepository;
import com.chatbot.modules.tenant.membership.model.TenantMember;
import com.chatbot.modules.tenant.membership.model.TenantRole;
import com.chatbot.modules.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.modules.tenant.core.util.SecurityContextUtil;
import com.chatbot.modules.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Tenant service for v0.1
 * Core tenant management functionality
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMemberRepository tenantMemberRepository;
    private final SecurityContextUtil securityContextUtil;

    /**
     * Get all tenants for the current user
     */
    @Transactional(readOnly = true)
    public List<TenantResponse> getUserTenants() {
        UUID currentUserId = securityContextUtil.getCurrentUserIdUUID();
        
        if (currentUserId == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        log.info("Getting tenants for user: {}", currentUserId);
        List<Tenant> tenants = tenantRepository.findByUserId(currentUserId);
        
        return tenants.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new tenant
     * Creator becomes OWNER automatically
     */
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        return createTenantForUser(request, null, null);
    }

    /**
     * Create a new tenant for a specific user
     * This method is used during registration when user is not authenticated
     */
    @Transactional
    public TenantResponse createTenantForUser(CreateTenantRequest request, UUID userId, String userEmail) {
        // If userId and userEmail are not provided, try to get from SecurityContext
        UUID currentUserId = userId;
        String currentUserEmail = userEmail;
        
        if (currentUserId == null || currentUserEmail == null) {
            currentUserId = securityContextUtil.getCurrentUserIdUUID();
            currentUserEmail = securityContextUtil.getCurrentUserEmail();
            
            if (currentUserId == null || currentUserEmail == null) {
                throw new IllegalStateException("No authenticated user found and no user context provided");
            }
        }
        
        log.info("Creating tenant '{}' for user: {}", request.getName(), currentUserId);
        
        Tenant tenant = Tenant.builder()
                .name(request.getName())
                .status(TenantStatus.ACTIVE)
                .visibility(request.getVisibility() != null ? request.getVisibility() : TenantVisibility.PUBLIC)
                .build();
        
        tenant = tenantRepository.save(tenant);
        log.info("Created tenant: {} for user: {}", tenant.getId(), currentUserId);

        // Creator becomes OWNER
        TenantMember member = TenantMember.builder()
                .tenant(tenant)
                .userId(currentUserId)
                .email(currentUserEmail)
                .role(TenantRole.OWNER)
                .build();
        
        tenantMemberRepository.save(member);
        log.info("Created owner membership for tenant: {} user: {}", tenant.getId(), currentUserId);

        return mapToResponse(tenant);
    }

    /**
     * Switch active tenant context
     * Validates user has access to tenant
     */
    @Transactional(readOnly = true)
    public TenantResponse switchTenant(UUID tenantId) {
        UUID currentUserId = securityContextUtil.getCurrentUserIdUUID();
        
        if (currentUserId == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        log.info("Switching to tenant: {} for user: {}", tenantId, currentUserId);
        
        TenantMember membership = tenantMemberRepository
                .findByTenantIdAndUserId(tenantId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this tenant"));

        Tenant tenant = membership.getTenant();
        if (!tenant.isActive()) {
            throw new IllegalStateException("Tenant is not active");
        }

        return mapToResponse(tenant);
    }

    /**
     * Get tenant details
     * Validates user has access to tenant
     */
    @Transactional(readOnly = true)
    public TenantResponse getTenant(UUID tenantId) {
        UUID currentUserId = securityContextUtil.getCurrentUserIdUUID();
        
        if (currentUserId == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        log.info("Getting tenant: {} for user: {}", tenantId, currentUserId);
        
        TenantMember membership = tenantMemberRepository
                .findByTenantIdAndUserId(tenantId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this tenant"));

        return mapToResponse(membership.getTenant());
    }

    /**
     * Search tenants
     */
    @Transactional(readOnly = true)
    public List<TenantResponse> searchTenants(TenantSearchRequest request) {
        UUID currentUserId = securityContextUtil.getCurrentUserIdUUID();
        
        if (currentUserId == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        log.info("Searching tenants for user: {} with keyword: {}", currentUserId, request.getKeyword());
        
        List<Tenant> tenants;
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            tenants = tenantRepository.searchTenants(currentUserId, request.getKeyword().trim());
        } else {
            tenants = tenantRepository.findByUserId(currentUserId);
        }
        
        return tenants.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .status(tenant.getStatus())
                .createdAt(tenant.getCreatedAt())
                .build();
    }

    /**
     * Get current tenant ID from TenantContext
     * 
     * @return current tenant ID from X-Tenant-ID header
     * @throws IllegalStateException if TenantContext is empty (X-Tenant-ID header missing)
     */
    public UUID getCurrentTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException(
                "TenantContext is empty. X-Tenant-ID header is required for this operation."
            );
        }
        return tenantId;
    }
}
