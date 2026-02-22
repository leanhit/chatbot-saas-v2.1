package com.chatbot.core.wallet.transaction.controller;

import com.chatbot.core.wallet.transaction.dto.PurchaseRequest;
import com.chatbot.core.wallet.transaction.dto.TopupRequest;
import com.chatbot.core.wallet.transaction.dto.TransactionResponse;
import com.chatbot.core.wallet.transaction.service.TopupPurchaseService;
import com.chatbot.core.wallet.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;
    private final TopupPurchaseService topupPurchaseService;

    @PostMapping("/topup")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> topup(@RequestBody TopupRequest request) {
        log.info("Processing topup request for wallet: {}", request.getWalletId());
        TransactionResponse response = topupPurchaseService.processTopup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> purchase(@RequestBody PurchaseRequest request) {
        log.info("Processing purchase request for wallet: {}", request.getWalletId());
        TransactionResponse response = topupPurchaseService.processPurchase(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{transactionId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> refund(@PathVariable Long transactionId, @RequestBody String reason) {
        log.info("Processing refund for transaction: {}", transactionId);
        TransactionResponse response = topupPurchaseService.processRefund(transactionId, reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(@PathVariable Long walletId) {
        List<TransactionResponse> transactions = transactionService.getTransactionHistory(walletId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/wallet/{walletId}/paged")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponse> transactions = transactionService.getTransactionHistory(walletId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> getTransactionByReference(@PathVariable String reference) {
        TransactionResponse transaction = transactionService.getTransactionByReference(reference);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/{transactionId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> completeTransaction(@PathVariable Long transactionId) {
        TransactionResponse response = transactionService.completeTransaction(transactionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{transactionId}/fail")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> failTransaction(@PathVariable Long transactionId, @RequestBody String reason) {
        TransactionResponse response = transactionService.failTransaction(transactionId, reason);
        return ResponseEntity.ok(response);
    }
}
