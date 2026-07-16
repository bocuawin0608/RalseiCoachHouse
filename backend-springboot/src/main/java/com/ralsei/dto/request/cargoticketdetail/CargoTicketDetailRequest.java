package com.ralsei.dto.request.cargoticketdetail;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
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
/**
 * Represents the request payload for cargo ticket detail operations.
 */
public class CargoTicketDetailRequest {
    /** Existing row identifier; omitted when a new detail is added. */
    private Integer cargoTicketDetailId;

    @Min(value = 1, message = "Cargo type price ID must be greater than 0")
    private int cargoTypePriceId;

    private String description;

    @Min(value = 1, message = "Quantity must be greater than 0")
    private int quantity;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    private BigDecimal weightKg;

    @NotNull(message = "Dimension volume is required")
    @DecimalMin(value = "0.000001", message = "Dimension volume must be greater than 0")
    private BigDecimal dimensionVol;

}
