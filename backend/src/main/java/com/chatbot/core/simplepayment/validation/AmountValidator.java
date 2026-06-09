package com.chatbot.core.simplepayment.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class AmountValidator implements ConstraintValidator<ValidAmount, BigDecimal> {

    private double min;
    private double max;

    @Override
    public void initialize(ValidAmount constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        double amount = value.doubleValue();
        
        if (amount < min) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("Amount must be at least %,.0f VND", min)
            ).addConstraintViolation();
            return false;
        }

        if (amount > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("Amount must not exceed %,.0f VND", max)
            ).addConstraintViolation();
            return false;
        }

        // Check for positive value
        if (amount <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Amount must be positive"
            ).addConstraintViolation();
            return false;
        }

        // Check for reasonable decimal places (max 2)
        if (value.scale() > 2) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Amount must have at most 2 decimal places"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
