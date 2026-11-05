package com.chatbot.core.routing.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.model.AgentWorkload;
import com.chatbot.core.routing.model.ConversationTransfer;
import com.chatbot.core.routing.repository.AgentWorkloadRepository;
import com.chatbot.core.routing.repository.ConversationTransferRepository;
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
public class TransferService {

    private final ConversationTransferRepository transferRepository;
    private final ConversationRepository conversationRepository;
    private final AgentWorkloadRepository agentWorkloadRepository;

    @Transactional
    public ConversationTransferDTO createTransfer(CreateTransferRequest request) {
        log.info("Creating transfer for conversation {} from agent {} to agent {}",
                request.getConversationId(), request.getFromAgentId(), request.getToAgentId());

        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();

        // Validate conversation exists
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Validate agents exist and are available
        AgentWorkload fromAgent = agentWorkloadRepository.findByTenantIdAndAgentId(tenantId, request.getFromAgentId())
                .orElseThrow(() -> new RuntimeException("From agent not found"));

        AgentWorkload toAgent = agentWorkloadRepository.findByTenantIdAndAgentId(tenantId, request.getToAgentId())
                .orElseThrow(() -> new RuntimeException("To agent not found"));

        if (!toAgent.isAvailable()) {
            throw new IllegalStateException("Target agent is not available for transfer");
        }

        // Check if there's already a pending transfer for this conversation
        transferRepository.findByTenantIdAndConversationIdAndStatus(
                tenantId, request.getConversationId(), ConversationTransfer.TransferStatus.PENDING.name())
                .ifPresent(existing -> {
                    throw new IllegalStateException("Pending transfer already exists for this conversation");
                });

        ConversationTransfer transfer = ConversationTransfer.builder()
                .tenantId(tenantId)
                .conversationId(request.getConversationId())
                .fromAgentId(request.getFromAgentId())
                .toAgentId(request.getToAgentId())
                .transferReason(request.getTransferReason())
                .transferType(request.getTransferType().name())
                .status(ConversationTransfer.TransferStatus.PENDING.name())
                .notes(request.getNotes())
                .transferredBy(request.getTransferredBy())
                .build();

        ConversationTransfer saved = transferRepository.save(transfer);
        log.info("Transfer created successfully with ID: {}", saved.getId());

        return mapToDTO(saved);
    }

    @Transactional
    public ConversationTransferDTO acceptTransfer(Long transferId) {
        log.info("Accepting transfer with ID: {}", transferId);

        ConversationTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if (!ConversationTransfer.TransferStatus.PENDING.name().equals(transfer.getStatus())) {
            throw new IllegalStateException("Transfer is not in pending status");
        }

        transfer.setStatus(ConversationTransfer.TransferStatus.ACCEPTED.name());
        transfer.setAcceptedAt(LocalDateTime.now());

        // Update conversation assignment
        Conversation conversation = conversationRepository.findById(transfer.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.setAgentAssignedId(transfer.getToAgentId());
        conversationRepository.save(conversation);

        // Update agent workloads
        updateAgentWorkload(transfer.getFromAgentId(), -1);
        updateAgentWorkload(transfer.getToAgentId(), 1);

        ConversationTransfer updated = transferRepository.save(transfer);
        log.info("Transfer accepted successfully");

        return mapToDTO(updated);
    }

    @Transactional
    public ConversationTransferDTO rejectTransfer(Long transferId, String reason) {
        log.info("Rejecting transfer with ID: {}", transferId);

        ConversationTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if (!ConversationTransfer.TransferStatus.PENDING.name().equals(transfer.getStatus())) {
            throw new IllegalStateException("Transfer is not in pending status");
        }

        transfer.setStatus(ConversationTransfer.TransferStatus.REJECTED.name());
        transfer.setRejectedAt(LocalDateTime.now());
        if (reason != null) {
            transfer.setNotes(reason);
        }

        ConversationTransfer updated = transferRepository.save(transfer);
        log.info("Transfer rejected successfully");

        return mapToDTO(updated);
    }

    @Transactional
    public ConversationTransferDTO completeTransfer(Long transferId) {
        log.info("Completing transfer with ID: {}", transferId);

        ConversationTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if (!ConversationTransfer.TransferStatus.ACCEPTED.name().equals(transfer.getStatus())) {
            throw new IllegalStateException("Transfer must be accepted before completion");
        }

        transfer.setStatus(ConversationTransfer.TransferStatus.COMPLETED.name());
        transfer.setCompletedAt(LocalDateTime.now());

        ConversationTransfer updated = transferRepository.save(transfer);
        log.info("Transfer completed successfully");

        return mapToDTO(updated);
    }

    @Transactional
    public void cancelTransfer(Long transferId) {
        log.info("Cancelling transfer with ID: {}", transferId);

        ConversationTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if (ConversationTransfer.TransferStatus.COMPLETED.name().equals(transfer.getStatus())) {
            throw new IllegalStateException("Cannot cancel a completed transfer");
        }

        transfer.setStatus(ConversationTransfer.TransferStatus.CANCELLED.name());
        transferRepository.save(transfer);
        log.info("Transfer cancelled successfully");
    }

    @Transactional(readOnly = true)
    public ConversationTransferDTO getTransferById(Long transferId) {
        ConversationTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));
        return mapToDTO(transfer);
    }

    @Transactional(readOnly = true)
    public List<ConversationTransferDTO> getTransfersByConversation(Long conversationId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<ConversationTransfer> transfers = transferRepository
                .findByTenantIdAndConversationIdOrderByCreatedAtDesc(tenantId, conversationId);
        return transfers.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConversationTransferDTO> getTransfersByAgent(Long agentId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<ConversationTransfer> transfers = transferRepository
                .findByTenantIdAndFromAgentIdOrderByCreatedAtDesc(tenantId, agentId);
        transfers.addAll(transferRepository.findByTenantIdAndToAgentIdOrderByCreatedAtDesc(tenantId, agentId));
        return transfers.stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConversationTransferDTO> getPendingTransfers() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<ConversationTransfer> transfers = transferRepository
                .findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, ConversationTransfer.TransferStatus.PENDING.name());
        return transfers.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConversationTransferDTO> getAllTransfers() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        List<ConversationTransfer> transfers = transferRepository.findByTenantId(tenantId);
        return transfers.stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransferResult transferConversation(Long conversationId, Long fromAgentId, Long toAgentId, String reason) {
        log.info("Transferring conversation {} from agent {} to agent {}", conversationId, fromAgentId, toAgentId);

        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();

        try {
            // Validate conversation
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));

            // Validate target agent is available
            AgentWorkload toAgent = agentWorkloadRepository.findByTenantIdAndAgentId(tenantId, toAgentId)
                    .orElseThrow(() -> new RuntimeException("Target agent not found"));

            if (!toAgent.isAvailable()) {
                return TransferResult.builder()
                        .conversationId(conversationId)
                        .transferred(false)
                        .fromAgentId(fromAgentId)
                        .toAgentId(toAgentId)
                        .reason("Target agent is not available")
                        .build();
            }

            // Create transfer record
            CreateTransferRequest request = CreateTransferRequest.builder()
                    .conversationId(conversationId)
                    .fromAgentId(fromAgentId)
                    .toAgentId(toAgentId)
                    .transferReason(reason)
                    .transferType(ConversationTransfer.TransferType.MANUAL)
                    .build();

            ConversationTransferDTO transferDTO = createTransfer(request);

            // Auto-accept the transfer for immediate transfer
            acceptTransfer(transferDTO.getId());

            return TransferResult.builder()
                    .conversationId(conversationId)
                    .transferred(true)
                    .fromAgentId(fromAgentId)
                    .toAgentId(toAgentId)
                    .transferId(transferDTO.getId())
                    .transferType(transferDTO.getTransferType().name())
                    .status("COMPLETED")
                    .reason("Transfer completed successfully")
                    .build();

        } catch (Exception e) {
            log.error("Failed to transfer conversation {}", conversationId, e);
            return TransferResult.builder()
                    .conversationId(conversationId)
                    .transferred(false)
                    .fromAgentId(fromAgentId)
                    .toAgentId(toAgentId)
                    .reason("Transfer failed: " + e.getMessage())
                    .build();
        }
    }

    private void updateAgentWorkload(Long agentId, int delta) {
        try {
            Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
            AgentWorkload workload = agentWorkloadRepository.findByTenantIdAndAgentId(tenantId, agentId)
                    .orElse(null);
            if (workload != null) {
                workload.setActiveConversations(Math.max(0, workload.getActiveConversations() + delta));
                agentWorkloadRepository.save(workload);
                log.info("Updated workload for agent {}: {} active conversations", agentId, workload.getActiveConversations());
            }
        } catch (Exception e) {
            log.error("Failed to update workload for agent {}", agentId, e);
        }
    }

    private ConversationTransferDTO mapToDTO(ConversationTransfer transfer) {
        return ConversationTransferDTO.builder()
                .id(transfer.getId())
                .tenantId(transfer.getTenantId())
                .conversationId(transfer.getConversationId())
                .fromAgentId(transfer.getFromAgentId())
                .toAgentId(transfer.getToAgentId())
                .transferReason(transfer.getTransferReason())
                .transferType(ConversationTransfer.TransferType.valueOf(transfer.getTransferType()))
                .status(ConversationTransfer.TransferStatus.valueOf(transfer.getStatus()))
                .notes(transfer.getNotes())
                .transferredBy(transfer.getTransferredBy())
                .acceptedAt(transfer.getAcceptedAt())
                .rejectedAt(transfer.getRejectedAt())
                .completedAt(transfer.getCompletedAt())
                .createdAt(transfer.getCreatedAt())
                .build();
    }
}
