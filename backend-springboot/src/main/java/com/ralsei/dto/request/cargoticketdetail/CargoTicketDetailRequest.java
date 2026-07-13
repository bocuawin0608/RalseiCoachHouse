package com.ralsei.dto.request.cargoticketdetail;

import java.math.BigDecimal;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargoTicketDetailRequest {
    @Min(value = 1, message = "Cargo type price ID must be greater than 0")
    private int cargoTypePriceId;

    private String description;

    @Min(value = 1, message = "Quantity must be greater than 0")
    private int quantity;

    @NotNull(message = "Weight is required")
    private BigDecimal weightKg;

    @NotNull(message = "Dimension volume is required")
    private BigDecimal dimensionVol;

    @NotNull(message = "Calculated price is required")
    private BigDecimal calculatedPrice;
}
