package com.ralsei.util.validation;

import com.ralsei.repository.CargoTypeRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Provides the exists cargo type id validator component for the application.
 */
public class ExistsCargoTypeIdValidator implements ConstraintValidator<ExistsCargoTypeId, Integer> {

    private final CargoTypeRepository cargoTypeRepository;

    public ExistsCargoTypeIdValidator(CargoTypeRepository cargoTypeRepository) {
        this.cargoTypeRepository = cargoTypeRepository;
    }

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
            return true; // Use @NotNull for null checks
        }
        return cargoTypeRepository.existsById(value);
    }
}
