package com.ralsei.util.validation;

import java.time.Year;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Provides the max current year validator component for the application.
 */
public class MaxCurrentYearValidator implements ConstraintValidator<MaxCurrentYear, Integer> {

    @Override
    /**
     * Returns whether the valid is active.
     *
     * @param value the value supplied for this operation
     * @param context the value supplied for this operation
     *
     * @return {@code true} if the valid is active; otherwise {@code false}
     */
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value <= Year.now().getValue();
    }
}
