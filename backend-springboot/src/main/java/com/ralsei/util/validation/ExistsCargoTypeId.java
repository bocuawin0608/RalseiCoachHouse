package com.ralsei.util.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = ExistsCargoTypeIdValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistsCargoTypeId {
    String message() default "Cargo type does not exist";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
