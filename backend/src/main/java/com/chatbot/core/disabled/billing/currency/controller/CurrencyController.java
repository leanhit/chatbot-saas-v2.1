package com.chatbot.core.billing.currency.controller;

import com.chatbot.core.billing.currency.model.Currency;
import com.chatbot.core.billing.currency.model.ExchangeRate;
import com.chatbot.core.billing.currency.model.UserCurrencySettings;
import com.chatbot.core.billing.currency.service.CurrencyService;
import com.chatbot.core.tenant.service.TenantService;
import com.chatbot.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing/currency")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Currency Management", description = "APIs for currency conversion and settings")
public class CurrencyController {

    private final CurrencyService currencyService;
    private final TenantService tenantService;

    @Operation(summary = "Convert amount between currencies")
    @PostMapping("/convert")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMemberByKey(#tenantKey)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> convertCurrency(
            @Parameter(description = "Tenant Key") @RequestParam String tenantKey,
            @Parameter(description = "Amount to convert") @RequestParam BigDecimal amount,
            @Parameter(description = "Source currency") @RequestParam Currency from,
            @Parameter(description = "Target currency") @RequestParam Currency to) {
        
        log.info("Converting {} {} from {} to {}", amount, from, to);
        
        BigDecimal convertedAmount = currencyService.convert(amount, from, to);
        BigDecimal exchangeRate = currencyService.getExchangeRate(from, to);
        
        Map<String, Object> result = new HashMap<>();
        result.put("originalAmount", amount);
        result.put("convertedAmount", convertedAmount);
        result.put("fromCurrency", from);
        result.put("toCurrency", to);
        result.put("exchangeRate", exchangeRate);
        result.put("formattedOriginal", currencyService.formatAmount(amount, from));
        result.put("formattedConverted", currencyService.formatAmount(convertedAmount, to));
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Get exchange rate")
    @GetMapping("/rate")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMemberByKey(#tenantKey)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExchangeRate(
            @Parameter(description = "Tenant Key") @RequestParam String tenantKey,
            @Parameter(description = "Source currency") @RequestParam Currency from,
            @Parameter(description = "Target currency") @RequestParam Currency to) {
        
        BigDecimal rate = currencyService.getExchangeRate(from, to);
        
        Map<String, Object> result = new HashMap<>();
        result.put("fromCurrency", from);
        result.put("toCurrency", to);
        result.put("rate", rate);
        result.put("formattedRate", "1 " + from.getSymbol() + " = " + rate + " " + to.getSymbol());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Get all exchange rates")
    @GetMapping("/rates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getAllExchangeRates() {
        List<ExchangeRate> rates = currencyService.getAllExchangeRates();
        return ResponseEntity.ok(ApiResponse.success(rates));
    }

    @Operation(summary = "Update exchange rate")
    @PostMapping("/rates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> updateExchangeRate(
            @Parameter(description = "Source currency") @RequestParam Currency from,
            @Parameter(description = "Target currency") @RequestParam Currency to,
            @Parameter(description = "Exchange rate") @RequestParam BigDecimal rate,
            @Parameter(description = "Source of rate") @RequestParam(defaultValue = "MANUAL") String source) {
        
        ExchangeRate updatedRate = currencyService.updateExchangeRate(from, to, rate, source);
        return ResponseEntity.ok(ApiResponse.success(updatedRate, "Exchange rate updated successfully"));
    }

    @Operation(summary = "Get user currency settings")
    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMemberByKey(#tenantKey)")
    public ResponseEntity<ApiResponse<Object>> getUserCurrencySettings(
            @Parameter(description = "Tenant Key") @RequestParam String tenantKey) {
        
        Long tenantId = tenantService.getTenantIdByKey(tenantKey);
        if (tenantId == null) {
            throw new RuntimeException("Tenant not found for tenant key: " + tenantKey);
        }
        
        Long userId = getCurrentUserId();
        UserCurrencySettings settings = currencyService.getUserCurrencySettings(userId, tenantId);
        
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @Operation(summary = "Update user currency settings")
    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMemberByKey(#tenantKey)")
    public ResponseEntity<ApiResponse<Object>> updateUserCurrencySettings(
            @Parameter(description = "Tenant Key") @RequestParam String tenantKey,
            @Valid @RequestBody UserCurrencySettingsRequest request) {
        
        Long tenantId = tenantService.getTenantIdByKey(tenantKey);
        if (tenantId == null) {
            throw new RuntimeException("Tenant not found for tenant key: " + tenantKey);
        }
        
        Long userId = getCurrentUserId();
        UserCurrencySettings settings = currencyService.updateUserCurrencySettings(
                userId, tenantId, 
                request.getDisplayCurrency(), 
                request.getPaymentCurrency(),
                request.getAutoConvert(),
                request.getShowOriginalPrice());
        
        return ResponseEntity.ok(ApiResponse.success(settings, "Currency settings updated successfully"));
    }

    @Operation(summary = "Get supported currencies")
    @GetMapping("/supported")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSupportedCurrencies() {
        List<Map<String, Object>> currencies = List.of(
                createCurrencyInfo(Currency.USD),
                createCurrencyInfo(Currency.VND),
                createCurrencyInfo(Currency.EUR),
                createCurrencyInfo(Currency.GBP),
                createCurrencyInfo(Currency.JPY)
        );
        
        return ResponseEntity.ok(ApiResponse.success(currencies));
    }

    private Map<String, Object> createCurrencyInfo(Currency currency) {
        Map<String, Object> info = new HashMap<>();
        info.put("code", currency.getCode());
        info.put("symbol", currency.getSymbol());
        info.put("displayName", currency.getDisplayName());
        info.put("numericCode", currency.getNumericCode());
        return info;
    }

    private Long getCurrentUserId() {
        // TODO: Get current user ID from security context
        // For now, return a placeholder
        return 1L;
    }

    // DTO for request body
    public static class UserCurrencySettingsRequest {
        private Currency displayCurrency;
        private Currency paymentCurrency;
        private Boolean autoConvert;
        private Boolean showOriginalPrice;

        // Getters and setters
        public Currency getDisplayCurrency() { return displayCurrency; }
        public void setDisplayCurrency(Currency displayCurrency) { this.displayCurrency = displayCurrency; }
        
        public Currency getPaymentCurrency() { return paymentCurrency; }
        public void setPaymentCurrency(Currency paymentCurrency) { this.paymentCurrency = paymentCurrency; }
        
        public Boolean getAutoConvert() { return autoConvert; }
        public void setAutoConvert(Boolean autoConvert) { this.autoConvert = autoConvert; }
        
        public Boolean getShowOriginalPrice() { return showOriginalPrice; }
        public void setShowOriginalPrice(Boolean showOriginalPrice) { this.showOriginalPrice = showOriginalPrice; }
    }
}
