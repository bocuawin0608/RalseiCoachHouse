package com.ralsei.util.validation;

import com.ralsei.repository.CargoTypeRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExistsCargoTypeIdValidator implements ConstraintValidator<ExistsCargoTypeId, Integer> {

    private final CargoTypeRepository cargoTypeRepository;

    public ExistsCargoTypeIdValidator(CargoTypeRepository cargoTypeRepository) {
        this.cargoTypeRepository = cargoTypeRepository;
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }
        return cargoTypeRepository.existsById(value);
    }
}
