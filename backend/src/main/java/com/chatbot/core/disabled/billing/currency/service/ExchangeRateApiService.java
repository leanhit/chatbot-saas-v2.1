package com.chatbot.core.billing.currency.service;

import com.chatbot.core.billing.currency.model.Currency;
import com.chatbot.core.billing.currency.model.ExchangeRate;
import com.chatbot.core.billing.currency.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateApiService {

    private final RestTemplate restTemplate;
    private final ExchangeRateRepository exchangeRateRepository;

    /**
     * Update exchange rates from external API
     */
    public void updateExchangeRatesFromApi() {
        try {
            log.info("Starting exchange rate update from API");
            
            // Update USD to VND rate (Vietcombank API or similar)
            updateUsdToVndRate();
            
            // Update other major currency pairs
            updateMajorCurrencyPairs();
            
            log.info("Exchange rate update completed");
        } catch (Exception e) {
            log.error("Failed to update exchange rates from API", e);
        }
    }

    /**
     * Update USD to VND rate from Vietcombank API
     */
    private void updateUsdToVndRate() {
        try {
            // Using a mock API call - replace with real API
            BigDecimal usdToVndRate = fetchUsdToVndFromApi();
            
            if (usdToVndRate != null && usdToVndRate.compareTo(BigDecimal.ZERO) > 0) {
                // Create and save exchange rate directly
                ExchangeRate usdToVnd = ExchangeRate.builder()
                        .fromCurrency(Currency.USD)
                        .toCurrency(Currency.VND)
                        .rate(usdToVndRate)
                        .source("API")
                        .isActive(true)
                        .build();
                exchangeRateRepository.save(usdToVnd);
                
                // Create inverse rate
                BigDecimal vndToUsdRate = BigDecimal.ONE.divide(usdToVndRate, 6, BigDecimal.ROUND_HALF_UP);
                ExchangeRate vndToUsd = ExchangeRate.builder()
                        .fromCurrency(Currency.VND)
                        .toCurrency(Currency.USD)
                        .rate(vndToUsdRate)
                        .source("API")
                        .isActive(true)
                        .build();
                exchangeRateRepository.save(vndToUsd);
                
                log.info("Updated USD/VND rate: {}", usdToVndRate);
            }
        } catch (Exception e) {
            log.error("Failed to update USD/VND rate", e);
        }
    }

    /**
     * Update major currency pairs (USD/EUR, USD/GBP, USD/JPY)
     */
    private void updateMajorCurrencyPairs() {
        try {
            // Mock data - replace with real API calls
            Map<Currency, BigDecimal> rates = fetchMajorCurrencyRates();
            
            for (Map.Entry<Currency, BigDecimal> entry : rates.entrySet()) {
                Currency targetCurrency = entry.getKey();
                BigDecimal rate = entry.getValue();
                
                if (rate.compareTo(BigDecimal.ZERO) > 0) {
                    // USD to target
                    ExchangeRate usdToTarget = ExchangeRate.builder()
                            .fromCurrency(Currency.USD)
                            .toCurrency(targetCurrency)
                            .rate(rate)
                            .source("API")
                            .isActive(true)
                            .build();
                    exchangeRateRepository.save(usdToTarget);
                    
                    // Target to USD (inverse)
                    BigDecimal inverseRate = BigDecimal.ONE.divide(rate, 6, BigDecimal.ROUND_HALF_UP);
                    ExchangeRate targetToUsd = ExchangeRate.builder()
                            .fromCurrency(targetCurrency)
                            .toCurrency(Currency.USD)
                            .rate(inverseRate)
                            .source("API")
                            .isActive(true)
                            .build();
                    exchangeRateRepository.save(targetToUsd);
                    
                    log.info("Updated USD/{} rate: {}", targetCurrency, rate);
                }
            }
        } catch (Exception e) {
            log.error("Failed to update major currency pairs", e);
        }
    }

    /**
     * Fetch USD to VND rate from API
     * TODO: Replace with real Vietcombank API integration
     */
    private BigDecimal fetchUsdToVndFromApi() {
        try {
            // Mock implementation - replace with real API call
            // Example: https://portal.vietcombank.com.vn/api/exchange-rates
            
            // For now, return a reasonable default rate
            return new BigDecimal("24500.00");
            
            // Real implementation would be something like:
            /*
            String url = "https://portal.vietcombank.com.vn/api/exchange-rates";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map> rates = (List<Map>) response.getBody().get("data");
                for (Map rateData : rates) {
                    if ("USD".equals(rateData.get("currencyCode"))) {
                        String buyRate = rateData.get("buy").toString().replace(",", "");
                        return new BigDecimal(buyRate);
                    }
                }
            }
            */
        } catch (Exception e) {
            log.error("Error fetching USD/VND rate from API", e);
        }
        
        return null;
    }

    /**
     * Fetch major currency rates from API
     * TODO: Replace with real API integration (e.g., European Central Bank, Open Exchange Rates)
     */
    private Map<Currency, BigDecimal> fetchMajorCurrencyRates() {
        Map<Currency, BigDecimal> rates = new HashMap<>();
        
        try {
            // Mock implementation - replace with real API call
            rates.put(Currency.EUR, new BigDecimal("0.92"));
            rates.put(Currency.GBP, new BigDecimal("0.79"));
            rates.put(Currency.JPY, new BigDecimal("149.50"));
            
            // Real implementation would be something like:
            /*
            String url = "https://api.exchangerate-api.com/v4/latest/USD";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> rateData = (Map<String, Object>) response.getBody().get("rates");
                
                if (rateData.get("EUR") != null) {
                    rates.put(Currency.EUR, new BigDecimal(rateData.get("EUR").toString()));
                }
                if (rateData.get("GBP") != null) {
                    rates.put(Currency.GBP, new BigDecimal(rateData.get("GBP").toString()));
                }
                if (rateData.get("JPY") != null) {
                    rates.put(Currency.JPY, new BigDecimal(rateData.get("JPY").toString()));
                }
            }
            */
        } catch (Exception e) {
            log.error("Error fetching major currency rates from API", e);
        }
        
        return rates;
    }

    /**
     * Get last update time for exchange rates
     */
    public LocalDateTime getLastUpdateTime() {
        try {
            List<ExchangeRate> rates = exchangeRateRepository.findByIsActiveTrue();
            if (rates.isEmpty()) {
                return null;
            }
            
            return rates.stream()
                    .map(ExchangeRate::getUpdatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error getting last update time", e);
            return null;
        }
    }

    /**
     * Check if exchange rates need updating
     */
    public boolean needsUpdate(int hours) {
        LocalDateTime lastUpdate = getLastUpdateTime();
        if (lastUpdate == null) {
            return true;
        }
        
        return lastUpdate.plusHours(hours).isBefore(LocalDateTime.now());
    }
}
