package com.chatbot.core.membership.event;

import com.chatbot.core.message.store.service.AgentProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener for TenantMember lifecycle events.
 * Triggers Agent auto-provisioning AFTER the membership transaction commits,
 * ensuring cross-datasource operations are decoupled from the main HTTP transaction.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberEventListener {

    private final AgentProvisioningService agentProvisioningService;

    /**
     * Triggered after a member is successfully activated (invitation accepted or join request approved).
     * Creates a default Agent profile in the message DB for the new member.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberActivated(MemberActivatedEvent event) {
        log.info("[MemberEventListener] MemberActivatedEvent received for userId={}, tenantId={}",
                event.getUserId(), event.getTenantId());
        try {
            agentProvisioningService.provisionAgent(event);
        } catch (Exception e) {
            log.error("[MemberEventListener] Failed to provision agent for userId={}, tenantId={}: {}",
                    event.getUserId(), event.getTenantId(), e.getMessage(), e);
        }
    }

    /**
     * Triggered after a member is removed or blocked from the tenant.
     * Deactivates the corresponding Agent profile so they no longer receive conversations.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberDeactivated(MemberDeactivatedEvent event) {
        log.info("[MemberEventListener] MemberDeactivatedEvent received for userId={}, tenantId={}",
                event.getUserId(), event.getTenantId());
        try {
            agentProvisioningService.deactivateAgent(event.getTenantId(), event.getUserId());
        } catch (Exception e) {
            log.error("[MemberEventListener] Failed to deactivate agent for userId={}, tenantId={}: {}",
                    event.getUserId(), event.getTenantId(), e.getMessage(), e);
        }
    }
}
