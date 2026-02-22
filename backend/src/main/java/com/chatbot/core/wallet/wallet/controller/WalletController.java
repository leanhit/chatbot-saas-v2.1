package com.chatbot.core.wallet.wallet.controller;

import com.chatbot.core.wallet.wallet.dto.BalanceDto;
import com.chatbot.core.wallet.wallet.dto.CreateWalletRequest;
import com.chatbot.core.wallet.wallet.dto.WalletResponse;
import com.chatbot.core.wallet.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<WalletResponse> createWallet(@RequestBody CreateWalletRequest request) {
        log.info("Creating wallet for user {} in tenant {}", request.getUserId(), request.getTenantId());
        WalletResponse response = walletService.createWallet(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/tenant/{tenantId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<WalletResponse>> getUserWallets(
            @PathVariable Long userId,
            @PathVariable Long tenantId) {
        List<WalletResponse> wallets = walletService.getUserWallets(userId, tenantId);
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/{walletId}/balance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<BalanceDto> getBalance(@PathVariable Long walletId) {
        BalanceDto balance = walletService.getBalance(walletId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/user/{userId}/tenant/{tenantId}/balances")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<BalanceDto>> getAllBalances(
            @PathVariable Long userId,
            @PathVariable Long tenantId) {
        List<BalanceDto> balances = walletService.getAllBalances(userId, tenantId);
        return ResponseEntity.ok(balances);
    }

    @PostMapping("/{walletId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> suspendWallet(@PathVariable Long walletId) {
        walletService.suspendWallet(walletId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{walletId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateWallet(@PathVariable Long walletId) {
        walletService.activateWallet(walletId);
        return ResponseEntity.ok().build();
    }
}
