package com.chatbot.core.tenant.service;

import com.chatbot.core.message.store.repository.AgentRepository;
import com.chatbot.core.message.store.repository.AutoAssignConfigRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.message.store.repository.RoutingRuleRepository;
import com.chatbot.core.message.store.repository.SLAConfigurationRepository;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import com.chatbot.core.simplepayment.repository.InvoiceRepository;
import com.chatbot.core.tenant.membership.repository.TenantInvitationRepository;
import com.chatbot.core.tenant.membership.repository.TenantJoinRequestRepository;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.profile.repository.TenantProfileRepository;
import com.chatbot.core.tenant.professional.repository.TenantProfessionalRepository;
import com.chatbot.core.tenant.repository.TenantAuditLogRepository;
import com.chatbot.core.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for cleaning up tenant-related data after soft delete
 * Implements cascade soft delete for all related entities
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantCleanupService {

    private final TenantMemberRepository tenantMemberRepository;
    private final TenantProfileRepository tenantProfileRepository;
    private final TenantProfessionalRepository tenantProfessionalRepository;
    private final TenantInvitationRepository tenantInvitationRepository;
    private final TenantJoinRequestRepository tenantJoinRequestRepository;
    private final TenantAuditLogRepository tenantAuditLogRepository;
    private final TenantRepository tenantRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AgentRepository agentRepository;
    private final RoutingRuleRepository routingRuleRepository;
    private final SLAConfigurationRepository slaConfigurationRepository;
    private final AutoAssignConfigRepository autoAssignConfigRepository;
    private final SimplePaymentRepository simplePaymentRepository;
    private final InvoiceRepository invoiceRepository;

    /**
     * Cleanup all related data for a deleted tenant
     * This should be called after tenant soft delete
     */
    @Transactional(value = "tenantTransactionManager", rollbackFor = Exception.class)
    public void cleanupTenantData(Long tenantId) {
        log.info("[TenantCleanupService] Starting cleanup for tenant: {}", tenantId);

        try {
            // Cleanup in order of dependency (child entities first)
            cleanupMessages(tenantId);
            cleanupConversations(tenantId);
            cleanupAgents(tenantId);
            cleanupRoutingRules(tenantId);
            cleanupSLAConfigurations(tenantId);
            cleanupAutoAssignConfig(tenantId);
            cleanupPayments(tenantId);
            cleanupInvoices(tenantId);
            cleanupTenantMembers(tenantId);
            cleanupTenantInvitations(tenantId);
            cleanupTenantJoinRequests(tenantId);
            cleanupTenantProfile(tenantId);
            cleanupTenantProfessional(tenantId);
            cleanupAuditLogs(tenantId);

            log.info("[TenantCleanupService] Successfully cleaned up data for tenant: {}", tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up data for tenant: {}", tenantId, e);
            throw e;
        }
    }

    /**
     * Scheduled job to clean up data for tenants deleted more than 30 days ago
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @SchedulerLock(name = "TenantCleanupService_scheduledCleanup", lockAtMostFor = "1h", lockAtLeastFor = "50m")
    @Transactional(value = "tenantTransactionManager", rollbackFor = Exception.class)
    public void scheduledCleanup() {
        log.info("[TenantCleanupService] Starting scheduled cleanup for old deleted tenants");
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        
        try {
            // Find all tenants deleted more than 30 days ago
            List<com.chatbot.core.tenant.model.Tenant> deletedTenants = 
                tenantRepository.findDeletedTenantsOlderThan(cutoffDate);
            
            if (deletedTenants.isEmpty()) {
                log.info("[TenantCleanupService] No tenants found for cleanup (deleted > 30 days ago)");
                return;
            }
            
            log.info("[TenantCleanupService] Found {} tenants for cleanup", deletedTenants.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            for (com.chatbot.core.tenant.model.Tenant tenant : deletedTenants) {
                try {
                    log.info("[TenantCleanupService] Cleaning up tenant: {} (deleted at: {})", 
                            tenant.getId(), tenant.getUpdatedAt());
                    
                    // Perform full cleanup for this tenant
                    cleanupTenantData(tenant.getId());
                    
                    // Optionally, hard delete the tenant record itself
                    // tenantRepository.delete(tenant);
                    
                    successCount++;
                    log.info("[TenantCleanupService] Successfully cleaned up tenant: {}", tenant.getId());
                    
                } catch (Exception e) {
                    failureCount++;
                    log.error("[TenantCleanupService] Failed to cleanup tenant: {}", tenant.getId(), e);
                    // Continue with next tenant even if one fails
                }
            }
            
            log.info("[TenantCleanupService] Scheduled cleanup completed - Success: {}, Failed: {}", 
                    successCount, failureCount);
                    
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error during scheduled cleanup", e);
            throw e;
        }
    }

    private void cleanupMessages(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up messages for tenant: {}", tenantId);
            messageRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up messages for tenant: {}", tenantId, e);
        }
    }

    private void cleanupConversations(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up conversations for tenant: {}", tenantId);
            conversationRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up conversations for tenant: {}", tenantId, e);
        }
    }

    private void cleanupAgents(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up agents for tenant: {}", tenantId);
            agentRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up agents for tenant: {}", tenantId, e);
        }
    }

    private void cleanupRoutingRules(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up routing rules for tenant: {}", tenantId);
            routingRuleRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up routing rules for tenant: {}", tenantId, e);
        }
    }

    private void cleanupSLAConfigurations(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up SLA configurations for tenant: {}", tenantId);
            slaConfigurationRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up SLA configurations for tenant: {}", tenantId, e);
        }
    }

    private void cleanupAutoAssignConfig(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up auto-assign config for tenant: {}", tenantId);
            autoAssignConfigRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up auto-assign config for tenant: {}", tenantId, e);
        }
    }

    private void cleanupPayments(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up payments for tenant: {}", tenantId);
            simplePaymentRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up payments for tenant: {}", tenantId, e);
        }
    }

    private void cleanupInvoices(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up invoices for tenant: {}", tenantId);
            invoiceRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up invoices for tenant: {}", tenantId, e);
        }
    }

    private void cleanupTenantMembers(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up tenant members for tenant: {}", tenantId);
            tenantMemberRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up tenant members for tenant: {}", tenantId, e);
        }
    }

    private void cleanupTenantInvitations(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up tenant invitations for tenant: {}", tenantId);
            tenantInvitationRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up tenant invitations for tenant: {}", tenantId, e);
        }
    }

    private void cleanupTenantJoinRequests(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up tenant join requests for tenant: {}", tenantId);
            tenantJoinRequestRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up tenant join requests for tenant: {}", tenantId, e);
        }
    }

    private void cleanupTenantProfile(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up tenant profile for tenant: {}", tenantId);
            tenantProfileRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up tenant profile for tenant: {}", tenantId, e);
        }
    }

    private void cleanupTenantProfessional(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up tenant professional for tenant: {}", tenantId);
            tenantProfessionalRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up tenant professional for tenant: {}", tenantId, e);
        }
    }

    private void cleanupAuditLogs(Long tenantId) {
        try {
            log.info("[TenantCleanupService] Cleaning up audit logs for tenant: {}", tenantId);
            tenantAuditLogRepository.deleteByTenantId(tenantId);
        } catch (Exception e) {
            log.error("[TenantCleanupService] Error cleaning up audit logs for tenant: {}", tenantId, e);
        }
    }
}
