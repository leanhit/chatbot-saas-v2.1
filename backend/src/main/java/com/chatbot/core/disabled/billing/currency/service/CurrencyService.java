package com.chatbot.core.billing.currency.service;

import com.chatbot.core.billing.currency.model.Currency;
import com.chatbot.core.billing.currency.model.ExchangeRate;
import com.chatbot.core.billing.currency.model.UserCurrencySettings;
import com.chatbot.core.billing.currency.repository.ExchangeRateRepository;
import com.chatbot.core.billing.currency.repository.UserCurrencySettingsRepository;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final UserCurrencySettingsRepository userCurrencySettingsRepository;

    /**
     * Convert amount from one currency to another
     */
    @Transactional(readOnly = true)
    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        if (from.equals(to)) {
            return amount;
        }

        ExchangeRate rate = exchangeRateRepository
                .findByFromCurrencyAndToCurrencyAndIsActiveTrue(from, to)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exchange rate not found for " + from + " to " + to));

        return amount.multiply(rate.getRate()).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Get exchange rate between two currencies
     */
    @Transactional(readOnly = true)
    public BigDecimal getExchangeRate(Currency from, Currency to) {
        if (from.equals(to)) {
            return BigDecimal.ONE;
        }

        return exchangeRateRepository
                .findByFromCurrencyAndToCurrencyAndIsActiveTrue(from, to)
                .map(ExchangeRate::getRate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Exchange rate not found for " + from + " to " + to));
    }

    /**
     * Update or create exchange rate
     */
    @Transactional
    public ExchangeRate updateExchangeRate(Currency from, Currency to, BigDecimal rate, String source) {
        log.info("Updating exchange rate: {} {} -> {} = {}", from, to, rate, source);

        Optional<ExchangeRate> existing = exchangeRateRepository
                .findByFromCurrencyAndToCurrency(from, to);

        ExchangeRate exchangeRate;
        if (existing.isPresent()) {
            exchangeRate = existing.get();
            exchangeRate.setRate(rate);
            exchangeRate.setSource(source);
            exchangeRate.setIsActive(true);
        } else {
            exchangeRate = ExchangeRate.builder()
                    .fromCurrency(from)
                    .toCurrency(to)
                    .rate(rate)
                    .source(source)
                    .isActive(true)
                    .build();
        }

        return exchangeRateRepository.save(exchangeRate);
    }

    /**
     * Get all active exchange rates
     */
    @Transactional(readOnly = true)
    public List<ExchangeRate> getAllExchangeRates() {
        return exchangeRateRepository.findByIsActiveTrue();
    }

    /**
     * Get user currency settings
     */
    @Transactional(readOnly = true)
    public UserCurrencySettings getUserCurrencySettings(Long userId, Long tenantId) {
        return userCurrencySettingsRepository
                .findByUserIdAndTenantId(userId, tenantId)
                .orElseGet(() -> createDefaultSettings(userId, tenantId));
    }

    /**
     * Update user currency settings
     */
    @Transactional
    public UserCurrencySettings updateUserCurrencySettings(Long userId, Long tenantId, 
            Currency displayCurrency, Currency paymentCurrency, Boolean autoConvert, Boolean showOriginalPrice) {
        
        UserCurrencySettings settings = userCurrencySettingsRepository
                .findByUserIdAndTenantId(userId, tenantId)
                .orElseGet(() -> {
                    UserCurrencySettings newSettings = new UserCurrencySettings();
                    newSettings.setUserId(userId);
                    newSettings.setTenantId(tenantId);
                    return newSettings;
                });

        settings.setDisplayCurrency(displayCurrency);
        settings.setPaymentCurrency(paymentCurrency);
        settings.setAutoConvert(autoConvert != null ? autoConvert : settings.getAutoConvert());
        settings.setShowOriginalPrice(showOriginalPrice != null ? showOriginalPrice : settings.getShowOriginalPrice());

        return userCurrencySettingsRepository.save(settings);
    }

    /**
     * Create default currency settings for user
     */
    private UserCurrencySettings createDefaultSettings(Long userId, Long tenantId) {
        UserCurrencySettings settings = UserCurrencySettings.builder()
                .userId(userId)
                .build();
        
        // Set tenantId using setter method
        settings.setTenantId(tenantId);
        settings.setDisplayCurrency(Currency.USD);
        settings.setPaymentCurrency(Currency.USD);
        settings.setAutoConvert(true);
        settings.setShowOriginalPrice(true);

        return userCurrencySettingsRepository.save(settings);
    }

    /**
     * Get exchange rates that need updating (older than specified hours)
     */
    @Transactional(readOnly = true)
    public List<ExchangeRate> getRatesNeedingUpdate(int olderThanHours) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(olderThanHours);
        return exchangeRateRepository.findRatesOlderThan(threshold);
    }

    /**
     * Deactivate exchange rate
     */
    @Transactional
    public void deactivateExchangeRate(Currency from, Currency to) {
        exchangeRateRepository.findByFromCurrencyAndToCurrency(from, to)
                .ifPresent(rate -> {
                    rate.setIsActive(false);
                    exchangeRateRepository.save(rate);
                    log.info("Deactivated exchange rate: {} -> {}", from, to);
                });
    }

    /**
     * Format amount with currency symbol
     */
    public String formatAmount(BigDecimal amount, Currency currency) {
        String symbol = currency.getSymbol();
        
        if (currency == Currency.VND) {
            return symbol + amount.setScale(0, RoundingMode.HALF_UP).longValue();
        } else {
            return symbol + amount.setScale(2, RoundingMode.HALF_UP);
        }
    }
}
