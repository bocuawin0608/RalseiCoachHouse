package com.ralsei.dto.response.cargotype;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned to staff cargo type management screens.
 *
 * <p>The price fields represent surcharge data from cargo type price, not the
 * freight-based base cargo price.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Represents the response payload for cargo type operations.
 */
public class CargoTypeResponse {
    private int cargoTypeId;
    private String cargoTypeName;
    private boolean isActive;
    private Integer cargoTypePriceId;
    private String unit;
    private BigDecimal pricePerUnit;
}
