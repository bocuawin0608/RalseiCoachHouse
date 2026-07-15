package com.ralsei.dto.response.cargotypeprice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Represents the response payload for cargo type price operations.
 */
public class CargoTypePriceResponse {
    private int cargoTypePriceId;
    private int cargoTypeId;
    private String unit;
    private BigDecimal pricePerUnit;
    private LocalDateTime startEffectiveDate;
    private LocalDateTime endEffectiveDate;
}
