package com.chatbot.shared.penny.escalation;

import com.chatbot.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * EscalationTicketController — Admin REST API for managing escalation tickets
 *
 * Provides CRUD operations for escalation tickets used in human handoff.
 * Requires ADMIN or TENANT_ADMIN role.
 */
@RestController
@RequestMapping("/api/penny/bots/{botId}/escalation")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN')")
public class EscalationTicketController {

    private final EscalationTicketRepository escalationTicketRepository;

    /**
     * Get all escalation tickets for a bot (paginated)
     * GET /api/penny/bots/{botId}/escalation/tickets?page=0&size=20
     */
    @GetMapping("/tickets")
    public ResponseEntity<Page<EscalationTicket>> getTickets(
            @PathVariable UUID botId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.debug("📋 Fetching escalation tickets for bot: {} (page: {}, size: {})", botId, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get tenant ID from security context
        Long tenantId = SecurityUtils.getCurrentTenantId()
            .orElseThrow(() -> new IllegalStateException("Tenant ID not found in security context"));
        
        Page<EscalationTicket> tickets = escalationTicketRepository
            .findByBotIdAndTenantId(botId, tenantId, pageable);
        
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get tickets by status for a bot
     * GET /api/penny/bots/{botId}/escalation/tickets/status/{status}
     */
    @GetMapping("/tickets/status/{status}")
    public ResponseEntity<List<EscalationTicket>> getTicketsByStatus(
            @PathVariable UUID botId,
            @PathVariable String status) {
        
        log.debug("📋 Fetching escalation tickets for bot: {} with status: {}", botId, status);

        Long tenantId = SecurityUtils.getCurrentTenantId()
            .orElseThrow(() -> new IllegalStateException("Tenant ID not found in security context"));
        List<EscalationTicket> tickets = escalationTicketRepository
            .findByBotIdAndTenantIdAndStatus(botId, tenantId, status);
        
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get a specific escalation ticket by ID
     * GET /api/penny/bots/{botId}/escalation/tickets/{id}
     */
    @GetMapping("/tickets/{id}")
    public ResponseEntity<EscalationTicket> getTicket(
            @PathVariable UUID botId,
            @PathVariable UUID id) {
        
        log.debug("📖 Fetching escalation ticket: {} for bot: {}", id, botId);
        
        return escalationTicketRepository.findById(id)
            .filter(ticket -> ticket.getBotId().equals(botId))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new escalation ticket
     * POST /api/penny/bots/{botId}/escalation/tickets
     */
    @PostMapping("/tickets")
    public ResponseEntity<EscalationTicket> createTicket(
            @PathVariable UUID botId,
            @RequestBody EscalationTicket ticket) {
        
        log.info("📝 Creating escalation ticket for bot: {}", botId);
        
        ticket.setBotId(botId);
        ticket.setId(UUID.randomUUID());
        
        // Set default status if not provided
        if (ticket.getStatus() == null || ticket.getStatus().isBlank()) {
            ticket.setStatus("PENDING");
        }
        
        // Set default priority if not provided
        if (ticket.getPriority() == null || ticket.getPriority().isBlank()) {
            ticket.setPriority("NORMAL");
        }
        
        EscalationTicket saved = escalationTicketRepository.save(ticket);
        log.info("✅ Created escalation ticket: {}", saved.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Update an escalation ticket
     * PUT /api/penny/bots/{botId}/escalation/tickets/{id}
     */
    @PutMapping("/tickets/{id}")
    public ResponseEntity<EscalationTicket> updateTicket(
            @PathVariable UUID botId,
            @PathVariable UUID id,
            @RequestBody EscalationTicket updatedTicket) {
        
        log.info("✏️ Updating escalation ticket: {} for bot: {}", id, botId);
        
        return escalationTicketRepository.findById(id)
            .filter(ticket -> ticket.getBotId().equals(botId))
            .map(existingTicket -> {
                // Update fields
                existingTicket.setReason(updatedTicket.getReason());
                existingTicket.setStatus(updatedTicket.getStatus());
                existingTicket.setPriority(updatedTicket.getPriority());
                existingTicket.setAssignedAgentId(updatedTicket.getAssignedAgentId());
                existingTicket.setMetadata(updatedTicket.getMetadata());
                
                // If resolving, set resolvedAt and resolutionNotes
                if ("RESOLVED".equals(updatedTicket.getStatus()) && existingTicket.getResolvedAt() == null) {
                    existingTicket.setResolvedAt(java.time.LocalDateTime.now());
                    existingTicket.setResolutionNotes(updatedTicket.getResolutionNotes());
                }
                
                EscalationTicket saved = escalationTicketRepository.save(existingTicket);
                log.info("✅ Updated escalation ticket: {}", saved.getId());
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete an escalation ticket
     * DELETE /api/penny/bots/{botId}/escalation/tickets/{id}
     */
    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable UUID botId,
            @PathVariable UUID id) {
        
        log.info("🗑️ Deleting escalation ticket: {} for bot: {}", id, botId);
        
        return escalationTicketRepository.findById(id)
            .filter(ticket -> ticket.getBotId().equals(botId))
            .map(ticket -> {
                escalationTicketRepository.deleteById(id);
                log.info("✅ Deleted escalation ticket: {}", id);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Assign ticket to an agent
     * POST /api/penny/bots/{botId}/escalation/tickets/{id}/assign
     */
    @PostMapping("/tickets/{id}/assign")
    public ResponseEntity<EscalationTicket> assignTicket(
            @PathVariable UUID botId,
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        
        String agentId = request.get("agentId");
        if (agentId == null || agentId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("👤 Assigning ticket {} to agent: {}", id, agentId);
        
        return escalationTicketRepository.findById(id)
            .filter(ticket -> ticket.getBotId().equals(botId))
            .map(ticket -> {
                ticket.markAsAssigned(agentId);
                EscalationTicket saved = escalationTicketRepository.save(ticket);
                log.info("✅ Assigned ticket {} to agent: {}", id, agentId);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Resolve a ticket
     * POST /api/penny/bots/{botId}/escalation/tickets/{id}/resolve
     */
    @PostMapping("/tickets/{id}/resolve")
    public ResponseEntity<EscalationTicket> resolveTicket(
            @PathVariable UUID botId,
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        
        String notes = request.get("notes");
        
        log.info("✅ Resolving ticket: {} for bot: {}", id, botId);
        
        return escalationTicketRepository.findById(id)
            .filter(ticket -> ticket.getBotId().equals(botId))
            .map(ticket -> {
                ticket.markAsResolved(notes);
                EscalationTicket saved = escalationTicketRepository.save(ticket);
                log.info("✅ Resolved ticket: {}", id);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cancel a ticket
     * POST /api/penny/bots/{botId}/escalation/tickets/{id}/cancel
     */
    @PostMapping("/tickets/{id}/cancel")
    public ResponseEntity<EscalationTicket> cancelTicket(
            @PathVariable UUID botId,
            @PathVariable UUID id) {
        
        log.info("❌ Cancelling ticket: {} for bot: {}", id, botId);
        
        return escalationTicketRepository.findById(id)
            .filter(ticket -> ticket.getBotId().equals(botId))
            .map(ticket -> {
                ticket.cancel();
                EscalationTicket saved = escalationTicketRepository.save(ticket);
                log.info("✅ Cancelled ticket: {}", id);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get pending tickets for a bot
     * GET /api/penny/bots/{botId}/escalation/tickets/pending
     */
    @GetMapping("/tickets/pending")
    public ResponseEntity<List<EscalationTicket>> getPendingTickets(
            @PathVariable UUID botId) {
        
        log.debug("📋 Fetching pending tickets for bot: {}", botId);

        Long tenantId = SecurityUtils.getCurrentTenantId()
            .orElseThrow(() -> new IllegalStateException("Tenant ID not found in security context"));
        List<EscalationTicket> tickets = escalationTicketRepository
            .findPendingTickets(botId, tenantId);
        
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get statistics for escalation tickets
     * GET /api/penny/bots/{botId}/escalation/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @PathVariable UUID botId) {
        
        log.debug("📊 Fetching escalation stats for bot: {}", botId);

        Long tenantId = SecurityUtils.getCurrentTenantId()
            .orElseThrow(() -> new IllegalStateException("Tenant ID not found in security context"));
        
        long totalTickets = escalationTicketRepository.countByBotIdAndTenantId(botId, tenantId);
        long pendingTickets = escalationTicketRepository.countByBotIdAndTenantIdAndStatus(botId, tenantId, "PENDING");
        long assignedTickets = escalationTicketRepository.countByBotIdAndTenantIdAndStatus(botId, tenantId, "ASSIGNED");
        long resolvedTickets = escalationTicketRepository.countByBotIdAndTenantIdAndStatus(botId, tenantId, "RESOLVED");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTickets", totalTickets);
        stats.put("pendingTickets", pendingTickets);
        stats.put("assignedTickets", assignedTickets);
        stats.put("resolvedTickets", resolvedTickets);
        stats.put("cancelledTickets", totalTickets - pendingTickets - assignedTickets - resolvedTickets);
        
        return ResponseEntity.ok(stats);
    }
}
