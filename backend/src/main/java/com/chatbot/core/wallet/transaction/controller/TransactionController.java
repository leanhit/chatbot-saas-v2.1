package com.chatbot.core.wallet.transaction.controller;

import com.chatbot.core.wallet.transaction.dto.PurchaseRequest;
import com.chatbot.core.wallet.transaction.dto.TopupRequest;
import com.chatbot.core.wallet.transaction.dto.TransactionResponse;
import com.chatbot.core.wallet.transaction.service.TopupPurchaseService;
import com.chatbot.core.wallet.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // Use fully qualified name to avoid conflict
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Transaction Management", description = "Transaction processing and history operations")
public class TransactionController {

    private final TransactionService transactionService;
    private final TopupPurchaseService topupPurchaseService;

    @PostMapping("/topup")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Top up wallet",
        description = "Add funds to a wallet",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Topup processed successfully",
                content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid topup amount"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    public ResponseEntity<TransactionResponse> topup(@RequestBody TopupRequest request) {
        log.info("Processing topup request for wallet: {}", request.getWalletId());
        TransactionResponse response = topupPurchaseService.processTopup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Process purchase",
        description = "Process a purchase transaction",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Purchase processed successfully",
                content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Insufficient funds or invalid purchase"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    public ResponseEntity<TransactionResponse> purchase(@RequestBody PurchaseRequest request) {
        log.info("Processing purchase request for wallet: {}", request.getWalletId());
        TransactionResponse response = topupPurchaseService.processPurchase(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{transactionId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Refund transaction",
        description = "Refund a completed transaction (admin only)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Refund processed successfully",
                content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found")
        }
    )
    public ResponseEntity<TransactionResponse> refund(@PathVariable Long transactionId, @RequestBody String reason) {
        log.info("Processing refund for transaction: {}", transactionId);
        TransactionResponse response = topupPurchaseService.processRefund(transactionId, reason);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get transaction history",
        description = "Retrieve all transactions for a wallet",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully",
                content = @Content(schema = @Schema(implementation = List.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found")
        }
    )
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(@PathVariable Long walletId) {
        List<TransactionResponse> transactions = transactionService.getTransactionHistory(walletId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/wallet/{walletId}/paged")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get paginated transaction history",
        description = "Retrieve paginated transactions for a wallet",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated transactions retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Wallet not found")
        }
    )
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
    @Operation(
        summary = "Get transaction by reference",
        description = "Retrieve a specific transaction by its reference ID",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction retrieved successfully",
                content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found")
        }
    )
    public ResponseEntity<TransactionResponse> getTransactionByReference(@PathVariable String reference) {
        TransactionResponse transaction = transactionService.getTransactionByReference(reference);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/{transactionId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Complete transaction",
        description = "Mark a pending transaction as completed (admin only)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction completed successfully",
                content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found")
        }
    )
    public ResponseEntity<TransactionResponse> completeTransaction(@PathVariable Long transactionId) {
        TransactionResponse response = transactionService.completeTransaction(transactionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{transactionId}/fail")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Fail transaction",
        description = "Mark a pending transaction as failed (admin only)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction marked as failed",
                content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found")
        }
    )
    public ResponseEntity<TransactionResponse> failTransaction(@PathVariable Long transactionId, @RequestBody String reason) {
        TransactionResponse response = transactionService.failTransaction(transactionId, reason);
        return ResponseEntity.ok(response);
    }
}
