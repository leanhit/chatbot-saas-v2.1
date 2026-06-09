package com.chatbot.core.simplepayment.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ReferenceCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidReferenceCode {
    
    String message() default "Invalid reference code";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
