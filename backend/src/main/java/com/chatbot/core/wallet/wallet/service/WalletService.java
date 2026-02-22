package com.chatbot.core.wallet.wallet.service;

import com.chatbot.core.wallet.wallet.dto.BalanceDto;
import com.chatbot.core.wallet.wallet.dto.CreateWalletRequest;
import com.chatbot.core.wallet.wallet.dto.WalletResponse;
import com.chatbot.core.wallet.wallet.model.Wallet;
import com.chatbot.core.wallet.wallet.model.WalletStatus;
import com.chatbot.core.wallet.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WalletService {

    private final WalletRepository walletRepository;
    private final BalanceService balanceService;

    public WalletResponse createWallet(CreateWalletRequest request) {
        log.info("Creating wallet for user {} in tenant {} with currency {}", 
                request.getUserId(), request.getTenantId(), request.getCurrency());

        // Check if wallet already exists
        walletRepository.findByUserIdAndTenantIdAndCurrency(
                request.getUserId(), request.getTenantId(), request.getCurrency())
                .ifPresent(wallet -> {
                    throw new IllegalStateException("Wallet already exists for this user, tenant, and currency");
                });

        Wallet wallet = Wallet.builder()
                .userId(request.getUserId())
                .tenantId(request.getTenantId())
                .currency(request.getCurrency())
                .balance(BigDecimal.ZERO)
                .status(WalletStatus.ACTIVE)
                .isActive(true)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Created wallet with ID: {}", savedWallet.getId());
        
        return toWalletResponse(savedWallet);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> getUserWallets(Long userId, Long tenantId) {
        List<Wallet> wallets = walletRepository.findActiveWalletsByUserAndTenant(userId, tenantId);
        return wallets.stream()
                .map(this::toWalletResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BalanceDto getBalance(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        return BalanceDto.from(wallet.getBalance(), wallet.getCurrency(), wallet.getStatus().name());
    }

    @Transactional(readOnly = true)
    public List<BalanceDto> getAllBalances(Long userId, Long tenantId) {
        List<Wallet> wallets = walletRepository.findActiveWalletsByUserAndTenant(userId, tenantId);
        return wallets.stream()
                .map(wallet -> BalanceDto.from(wallet.getBalance(), wallet.getCurrency(), wallet.getStatus().name()))
                .collect(Collectors.toList());
    }

    public void suspendWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.setStatus(WalletStatus.SUSPENDED);
        wallet.setIsActive(false);
        walletRepository.save(wallet);
        
        log.info("Suspended wallet with ID: {}", walletId);
    }

    public void activateWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setIsActive(true);
        walletRepository.save(wallet);
        
        log.info("Activated wallet with ID: {}", walletId);
    }

    private WalletResponse toWalletResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setTenantId(wallet.getTenantId());
        response.setBalance(wallet.getBalance());
        response.setCurrency(wallet.getCurrency());
        response.setStatus(wallet.getStatus().name());
        response.setIsActive(wallet.getIsActive());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());
        return response;
    }
}
