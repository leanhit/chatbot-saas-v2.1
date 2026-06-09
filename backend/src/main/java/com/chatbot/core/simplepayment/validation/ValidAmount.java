package com.chatbot.core.simplepayment.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AmountValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAmount {
    
    String message() default "Invalid payment amount";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    double min() default 10000.0;
    
    double max() default 50000000.0;
}
