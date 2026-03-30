package com.chatbot.core.billing.currency.repository;

import com.chatbot.core.billing.currency.model.Currency;
import com.chatbot.core.billing.currency.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    /**
     * Find exchange rate by currencies
     */
    Optional<ExchangeRate> findByFromCurrencyAndToCurrencyAndIsActiveTrue(
            Currency fromCurrency, Currency toCurrency);

    /**
     * Find all active exchange rates for a currency
     */
    List<ExchangeRate> findByFromCurrencyAndIsActiveTrue(Currency fromCurrency);

    /**
     * Find all active exchange rates
     */
    List<ExchangeRate> findByIsActiveTrue();

    /**
     * Find exchange rates updated before specified time
     */
    @Query("SELECT er FROM ExchangeRate er WHERE er.updatedAt < :threshold AND er.isActive = true")
    List<ExchangeRate> findRatesOlderThan(@Param("threshold") LocalDateTime threshold);

    /**
     * Check if exchange rate exists
     */
    boolean existsByFromCurrencyAndToCurrencyAndIsActiveTrue(
            Currency fromCurrency, Currency toCurrency);

    /**
     * Find exchange rate by currencies (including inactive)
     */
    Optional<ExchangeRate> findByFromCurrencyAndToCurrency(
            Currency fromCurrency, Currency toCurrency);
}
