package com.chatbot.core.tenant.service;

import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.simplepayment.service.PackageService;
import com.chatbot.shared.penny.repository.PennyBotRepository;
import com.chatbot.core.tenant.exception.BusinessLogicException;
import com.chatbot.core.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageLimitValidationService {
    
    private final TenantPackageService tenantPackageService;
    private final PackageService packageService;
    private final PennyBotRepository pennyBotRepository;
    private final TenantRepository tenantRepository;
    
    /**
     * Check if tenant can create more chatbots based on their Simple Payment package
     */
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public boolean canCreateMoreChatbots(Long tenantId) {
        log.info("[STEP 1] Starting chatbot limit validation for tenant: {}", tenantId);
        
        // Get current package for tenant from Simple Payment System
        log.info("[STEP 2] Getting current package for tenant: {}", tenantId);
        Package currentPackage = tenantPackageService.getCurrentTenantPackage(tenantId);
        if (currentPackage == null) {
            log.warn("[STEP 2] Tenant {} has no package assigned", tenantId);
            return false;
        }
        
        log.info("[STEP 2] Found package: {} (ID: {}) with chatbot limit: {}", 
                currentPackage.getName(), currentPackage.getPackageId(), currentPackage.getChatbotLimit());
        
        // Get current chatbot count for this tenant (CORRECT: count actual chatbots)
        log.info("[STEP 3] Counting current active chatbots for tenant: {}", tenantId);
        Long currentChatbotCount = pennyBotRepository.countByTenantIdAndIsActiveTrue(tenantId);
        
        log.info("[STEP 3] Current chatbot count: {}, Package limit: {} for tenant: {}", 
                currentChatbotCount, currentPackage.getChatbotLimit(), tenantId);
        
        // Check if unlimited
        if (currentPackage.getChatbotLimit() == -1 || currentPackage.getChatbotLimit() >= Integer.MAX_VALUE) {
            log.info("[STEP 4] Tenant {} has unlimited chatbots package - ALLOWING creation", tenantId);
            return true;
        }
        
        // Check if can create more
        boolean canCreate = currentChatbotCount < currentPackage.getChatbotLimit();
        int remaining = currentPackage.getChatbotLimit() - currentChatbotCount.intValue();
        
        if (canCreate) {
            log.info("[STEP 4] Tenant {} CAN create more chatbots. Current: {}, Limit: {}, Remaining: {}", 
                    tenantId, currentChatbotCount, currentPackage.getChatbotLimit(), remaining);
        } else {
            log.warn("[STEP 4] Tenant {} CANNOT create more chatbots. Current: {}, Limit: {}, Remaining: {}", 
                    tenantId, currentChatbotCount, currentPackage.getChatbotLimit(), remaining);
        }
        
        return canCreate;
    }
    
    /**
     * Get remaining chatbot slots for tenant
     */
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public int getRemainingChatbotSlots(Long tenantId) {
        Package currentPackage = tenantPackageService.getCurrentTenantPackage(tenantId);
        if (currentPackage == null) {
            return 0;
        }
        
        // If unlimited, return large number
        if (currentPackage.getChatbotLimit() == -1 || currentPackage.getChatbotLimit() >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        
        // Get current chatbot count (CORRECT: count actual chatbots)
        Long currentChatbotCount = pennyBotRepository.countByTenantIdAndIsActiveTrue(tenantId);
        int remaining = currentPackage.getChatbotLimit() - currentChatbotCount.intValue();
        
        return Math.max(0, remaining);
    }
    
    /**
     * Validate chatbot creation and throw exception if limit exceeded
     */
    @Transactional(transactionManager = "tenantTransactionManager")
    public void validateChatbotCreation(Long tenantId) {
        log.info("[VALIDATION START] Starting chatbot creation validation for tenant: {}", tenantId);
        
        // Acquire pessimistic write lock to prevent TOCTOU
        tenantRepository.findByIdWithPessimisticWriteLock(tenantId)
            .orElseThrow(() -> new BusinessLogicException("Tenant not found: " + tenantId));
            
        // Get package and count once to avoid duplicate DB calls
        Package currentPackage = tenantPackageService.getCurrentTenantPackage(tenantId);
        if (currentPackage == null) {
            log.warn("[VALIDATION FAILED] Tenant {} has no package assigned", tenantId);
            throw new BusinessLogicException("No package assigned to tenant. Please contact support.");
        }
        
        Long currentChatbotCount = pennyBotRepository.countByTenantIdAndIsActiveTrue(tenantId);
        
        // Check if unlimited
        if (currentPackage.getChatbotLimit() == -1 || currentPackage.getChatbotLimit() >= Integer.MAX_VALUE) {
            log.info("[VALIDATION PASSED] Tenant {} has unlimited chatbots package", tenantId);
            return;
        }
        
        // Check if can create more
        boolean canCreate = currentChatbotCount < currentPackage.getChatbotLimit();
        int remaining = currentPackage.getChatbotLimit() - currentChatbotCount.intValue();
        
        if (!canCreate) {
            String message = String.format(
                "Chatbot limit exceeded! Your %s package allows %d chatbots. You currently have %d chatbots. Remaining: %d", 
                currentPackage.getName(), 
                currentPackage.getChatbotLimit(), 
                currentChatbotCount,
                Math.max(0, remaining)
            );
            log.error("[LIMIT EXCEEDED] Tenant {} - Package: {}, Limit: {}, Current: {}, Remaining: {}", 
                    tenantId, currentPackage.getName(), currentPackage.getChatbotLimit(), 
                    currentChatbotCount, Math.max(0, remaining));
            throw new BusinessLogicException(message);
        }
        
        log.info("[VALIDATION PASSED] Tenant {} can create more chatbots. Current: {}, Limit: {}, Remaining: {}", 
                tenantId, currentChatbotCount, currentPackage.getChatbotLimit(), remaining);
    }
    
    /**
     * Get chatbot limit information for tenant
     */
    @Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
    public ChatbotLimitInfo getChatbotLimitInfo(Long tenantId) {
        Package currentPackage = tenantPackageService.getCurrentTenantPackage(tenantId);
        if (currentPackage == null) {
            return ChatbotLimitInfo.builder()
                    .tenantId(tenantId)
                    .packageName("No Package")
                    .packageId("none")
                    .totalLimit(0)
                    .currentCount(0)
                    .remainingSlots(0)
                    .canCreateMore(false)
                    .isUnlimited(false)
                    .build();
        }
        
        // Get current chatbot count (CORRECT: count actual chatbots)
        Long currentChatbotCount = pennyBotRepository.countByTenantIdAndIsActiveTrue(tenantId);
        boolean isUnlimited = currentPackage.getChatbotLimit() == -1 || currentPackage.getChatbotLimit() >= Integer.MAX_VALUE;
        int remaining;
        
        if (isUnlimited) {
            remaining = Integer.MAX_VALUE;
        } else {
            remaining = currentPackage.getChatbotLimit() - currentChatbotCount.intValue();
        }
        
        return ChatbotLimitInfo.builder()
                .tenantId(tenantId)
                .packageName(currentPackage.getName())
                .packageId(currentPackage.getPackageId())
                .totalLimit(currentPackage.getChatbotLimit())
                .currentCount(currentChatbotCount.intValue())
                .remainingSlots(Math.max(0, remaining))
                .canCreateMore(isUnlimited || remaining > 0)
                .isUnlimited(isUnlimited)
                .build();
    }
    
    /**
     * DTO for chatbot limit information
     */
    @lombok.Data
    @lombok.Builder
    public static class ChatbotLimitInfo {
        private Long tenantId;
        private String packageName;
        private String packageId;
        private Integer totalLimit;
        private Integer currentCount;
        private Integer remainingSlots;
        private Boolean canCreateMore;
        private Boolean isUnlimited;
    }
}
