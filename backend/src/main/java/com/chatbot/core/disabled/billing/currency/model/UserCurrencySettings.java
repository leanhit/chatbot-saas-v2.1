package com.chatbot.core.billing.currency.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * User currency preferences and settings
 */
@Entity
@Table(name = "user_currency_settings",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "tenant_id"})
       })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCurrencySettings extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_currency", nullable = false)
    @Builder.Default
    private Currency displayCurrency = Currency.USD;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_currency", nullable = false)
    @Builder.Default
    private Currency paymentCurrency = Currency.USD;

    @Column(name = "auto_convert", nullable = false)
    @Builder.Default
    private Boolean autoConvert = true;

    @Column(name = "show_original_price", nullable = false)
    @Builder.Default
    private Boolean showOriginalPrice = true;

    @Override
    public Object getId() {
        return id;
    }

    // Helper methods
    public boolean shouldConvert(Currency fromCurrency) {
        return autoConvert && !fromCurrency.equals(displayCurrency);
    }
}
