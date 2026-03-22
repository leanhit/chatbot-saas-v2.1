package com.chatbot.core.billing.currency.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Exchange rate entity for currency conversion
 */
@Entity
@Table(name = "currency_exchange_rates",
       indexes = {
           @Index(name = "idx_exchange_from_to", columnList = "from_currency, to_currency"),
           @Index(name = "idx_exchange_updated", columnList = "updated_at")
       })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_currency", nullable = false)
    private Currency fromCurrency;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_currency", nullable = false)
    private Currency toCurrency;

    @Column(name = "rate", precision = 19, scale = 6, nullable = false)
    private BigDecimal rate;

    @Column(name = "source", nullable = false)
    private String source; // "API", "MANUAL", "BANK"

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange rate must be greater than 0");
        }
    }

    @Override
    public Object getId() {
        return id;
    }

    // Helper methods
    public BigDecimal convert(BigDecimal amount) {
        return amount.multiply(rate);
    }

    public boolean isExpired(int hours) {
        return getUpdatedAt().plusHours(hours).isBefore(LocalDateTime.now());
    }
}
