package com.chatbot.core.wallet.wallet.controller;

import com.chatbot.core.wallet.wallet.dto.BalanceDto;
import com.chatbot.core.wallet.wallet.dto.CreateWalletRequest;
import com.chatbot.core.wallet.wallet.dto.WalletResponse;
import com.chatbot.core.wallet.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // Use fully qualified name to avoid conflict
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Wallet Management", description = "Digital wallet and balance management operations")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Create new wallet",
        description = "Create a new digital wallet for a user in a specific tenant",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wallet created successfully",
                content = @Content(schema = @Schema(implementation = WalletResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    public ResponseEntity<WalletResponse> createWallet(@RequestBody CreateWalletRequest request) {
        log.info("Creating wallet for user {} in tenant {}", request.getUserId(), request.getTenantId());
        WalletResponse response = walletService.createWallet(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/tenant/{tenantId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get user wallets",
        description = "Retrieve all wallets for a specific user in a tenant",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wallets retrieved successfully",
                content = @Content(schema = @Schema(implementation = List.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or tenant not found")
        }
    )
    public ResponseEntity<List<WalletResponse>> getUserWallets(
            @PathVariable Long userId,
            @PathVariable Long tenantId) {
        List<WalletResponse> wallets = walletService.getUserWallets(userId, tenantId);
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/{walletId}/balance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get wallet balance",
        description = "Retrieve current balance for a specific wallet",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Balance retrieved successfully",
                content = @Content(schema = @Schema(implementation = BalanceDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found")
        }
    )
    public ResponseEntity<BalanceDto> getBalance(@PathVariable Long walletId) {
        BalanceDto balance = walletService.getBalance(walletId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/user/{userId}/tenant/{tenantId}/balances")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get all user balances",
        description = "Retrieve all wallet balances for a user in a tenant",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Balances retrieved successfully",
                content = @Content(schema = @Schema(implementation = List.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User or tenant not found")
        }
    )
    public ResponseEntity<List<BalanceDto>> getAllBalances(
            @PathVariable Long userId,
            @PathVariable Long tenantId) {
        List<BalanceDto> balances = walletService.getAllBalances(userId, tenantId);
        return ResponseEntity.ok(balances);
    }

    @PostMapping("/{walletId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Suspend wallet",
        description = "Suspend a wallet (admin only)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wallet suspended successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found")
        }
    )
    public ResponseEntity<Void> suspendWallet(@PathVariable Long walletId) {
        walletService.suspendWallet(walletId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{walletId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Activate wallet",
        description = "Activate a suspended wallet (admin only)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Wallet activated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found")
        }
    )
    public ResponseEntity<Void> activateWallet(@PathVariable Long walletId) {
        walletService.activateWallet(walletId);
        return ResponseEntity.ok().build();
    }
}
