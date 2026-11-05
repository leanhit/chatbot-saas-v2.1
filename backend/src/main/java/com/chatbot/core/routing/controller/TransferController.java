package com.chatbot.core.routing.controller;

import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routing/transfers")
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<ConversationTransferDTO> createTransfer(@Valid @RequestBody CreateTransferRequest request) {
        ConversationTransferDTO transfer = transferService.createTransfer(request);
        return ResponseEntity.ok(transfer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationTransferDTO> getTransfer(@PathVariable Long id) {
        ConversationTransferDTO transfer = transferService.getTransferById(id);
        return ResponseEntity.ok(transfer);
    }

    @GetMapping
    public ResponseEntity<List<ConversationTransferDTO>> getAllTransfers() {
        List<ConversationTransferDTO> transfers = transferService.getAllTransfers();
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<ConversationTransferDTO>> getTransfersByConversation(@PathVariable Long conversationId) {
        List<ConversationTransferDTO> transfers = transferService.getTransfersByConversation(conversationId);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ConversationTransferDTO>> getTransfersByAgent(@PathVariable Long agentId) {
        List<ConversationTransferDTO> transfers = transferService.getTransfersByAgent(agentId);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ConversationTransferDTO>> getPendingTransfers() {
        List<ConversationTransferDTO> transfers = transferService.getPendingTransfers();
        return ResponseEntity.ok(transfers);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<ConversationTransferDTO> acceptTransfer(@PathVariable Long id) {
        ConversationTransferDTO transfer = transferService.acceptTransfer(id);
        return ResponseEntity.ok(transfer);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ConversationTransferDTO> rejectTransfer(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String reason = request != null ? request.get("reason") : null;
        ConversationTransferDTO transfer = transferService.rejectTransfer(id, reason);
        return ResponseEntity.ok(transfer);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ConversationTransferDTO> completeTransfer(@PathVariable Long id) {
        ConversationTransferDTO transfer = transferService.completeTransfer(id);
        return ResponseEntity.ok(transfer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTransfer(@PathVariable Long id) {
        transferService.cancelTransfer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResult> transferConversation(@RequestBody Map<String, Object> request) {
        Long conversationId = ((Number) request.get("conversationId")).longValue();
        Long fromAgentId = ((Number) request.get("fromAgentId")).longValue();
        Long toAgentId = ((Number) request.get("toAgentId")).longValue();
        String reason = request.containsKey("reason") ? (String) request.get("reason") : null;

        TransferResult result = transferService.transferConversation(conversationId, fromAgentId, toAgentId, reason);
        return ResponseEntity.ok(result);
    }
}
