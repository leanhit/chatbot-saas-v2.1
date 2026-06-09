package com.chatbot.core.simplepayment.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    private static final Set<String> ALLOWED_CURRENCIES = Set.of("VND", "USD", "EUR");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String currency = value.toUpperCase().trim();
        
        if (!ALLOWED_CURRENCIES.contains(currency)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("Currency must be one of: %s", ALLOWED_CURRENCIES)
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
