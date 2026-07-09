package com.ralsei.util.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = MaxCurrentYearValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxCurrentYear {
    String message() default "Năm sản xuất không được lớn hơn năm hiện tại.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
