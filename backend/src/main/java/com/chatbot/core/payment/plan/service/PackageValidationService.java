package com.chatbot.core.payment.plan.service;

import com.chatbot.core.payment.plan.model.Package;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageValidationService {

    private final PackageService packageService;

    /**
     * Validate package exists and is active
     */
    public boolean isValidPackage(String packageId) {
        if (packageId == null || packageId.trim().isEmpty()) {
            return false;
        }
        
        Optional<Package> packageOpt = packageService.getPackageByPackageId(packageId);
        return packageOpt.map(Package::getIsActive).orElse(false);
    }

    /**
     * Validate payment amount matches package price
     */
    public boolean isPaymentAmountValid(String packageId, BigDecimal amount) {
        if (packageId == null || amount == null) {
            return false;
        }
        
        Optional<Package> packageOpt = packageService.getPackageByPackageId(packageId);
        if (packageOpt.isEmpty()) {
            return false;
        }
        
        Package packageEntity = packageOpt.get();
        return amount.compareTo(packageEntity.getPrice()) == 0;
    }

    /**
     * Get package price
     */
    public BigDecimal getPackagePrice(String packageId) {
        Optional<Package> packageOpt = packageService.getPackageByPackageId(packageId);
        return packageOpt.map(Package::getPrice).orElse(BigDecimal.ZERO);
    }

    /**
     * Validate package for upgrade
     */
    public boolean canUpgradeToPackage(String currentPackageId, String targetPackageId) {
        if (targetPackageId == null || targetPackageId.trim().isEmpty()) {
            return false;
        }
        
        Optional<Package> targetPackageOpt = packageService.getPackageByPackageId(targetPackageId);
        if (targetPackageOpt.isEmpty() || !targetPackageOpt.get().getIsActive()) {
            return false;
        }
        
        // If current package is null (new tenant), allow upgrade to any active package
        if (currentPackageId == null || currentPackageId.trim().isEmpty()) {
            return true;
        }
        
        // Prevent downgrading
        Optional<Package> currentPackageOpt = packageService.getPackageByPackageId(currentPackageId);
        if (currentPackageOpt.isEmpty()) {
            return true;
        }
        
        Package currentPackage = currentPackageOpt.get();
        Package targetPackage = targetPackageOpt.get();
        
        // Allow upgrade if target price is higher or same (for renewals)
        return targetPackage.getPrice().compareTo(currentPackage.getPrice()) >= 0;
    }
}
