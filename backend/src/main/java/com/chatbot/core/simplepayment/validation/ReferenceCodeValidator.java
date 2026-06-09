package com.chatbot.core.simplepayment.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReferenceCodeValidator implements ConstraintValidator<ValidReferenceCode, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String referenceCode = value.trim().toUpperCase();
        
        // Reference code should start with "PAY" and be alphanumeric
        if (!referenceCode.matches("^PAY[A-Z0-9]{12}$")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Reference code must start with PAY followed by 12 alphanumeric characters"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
