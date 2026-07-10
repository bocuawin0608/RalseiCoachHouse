package com.ralsei.dto.request.cargotypeprice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargoTypePriceRequest {

    @NotNull(message = "Cargo type ID is required")
    @Min(value = 1, message = "Cargo type ID must be greater than or equal to 1")
    @com.ralsei.util.validation.ExistsCargoTypeId
    private int cargoTypeId;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Price per unit is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price per unit must be greater than or equal to 0")
    @DecimalMax(value = "9999999999999.99", message = "Price per unit must not exceed 9,999,999,999,999.99")
    @Digits(integer = 13, fraction = 2, message = "Price per unit supports up to 13 integer digits and 2 decimal digits")
    private BigDecimal pricePerUnit;

    @NotNull(message = "Start effective date is required")
    private LocalDateTime startEffectiveDate;

    @NotNull(message = "End effective date is required")
    private LocalDateTime endEffectiveDate;
}
