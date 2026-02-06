package com.chatbot.modules.wallet.core.controller;

import com.chatbot.modules.wallet.core.dto.WalletResponse;
import com.chatbot.modules.wallet.core.dto.WalletTransactionRequest;
import com.chatbot.modules.wallet.core.dto.WalletTransactionResponse;
import com.chatbot.modules.wallet.core.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Wallet controller for v0.1
 * Ledger-based wallet management endpoints
 */
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    /**
     * Get wallet information
     * Auto-creates wallet if not exists
     */
    @GetMapping("/{tenantId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID tenantId) {
        WalletResponse wallet = walletService.getWallet(tenantId);
        return ResponseEntity.ok(wallet);
    }

    /**
     * Get wallet transaction history
     * Returns empty list if wallet doesn't exist yet
     */
    @GetMapping("/{tenantId}/transactions")
    public ResponseEntity<List<WalletTransactionResponse>> getTransactions(@PathVariable UUID tenantId) {
        List<WalletTransactionResponse> transactions = walletService.getTransactions(tenantId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Add money to wallet (TOPUP)
     * Creates wallet if not exists
     */
    @PostMapping("/{tenantId}/topup")
    public ResponseEntity<WalletResponse> topup(
            @PathVariable UUID tenantId,
            @Valid @RequestBody WalletTransactionRequest request) {
        WalletResponse wallet = walletService.topup(tenantId, request);
        return ResponseEntity.ok(wallet);
    }

    /**
     * Deduct money from wallet (PURCHASE)
     * Fails if insufficient balance
     */
    @PostMapping("/{tenantId}/deduct")
    public ResponseEntity<WalletResponse> deduct(
            @PathVariable UUID tenantId,
            @Valid @RequestBody WalletTransactionRequest request) {
        WalletResponse wallet = walletService.deduct(tenantId, request);
        return ResponseEntity.ok(wallet);
    }
}
