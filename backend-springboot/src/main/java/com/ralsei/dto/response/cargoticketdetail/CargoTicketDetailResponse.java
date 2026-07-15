package com.ralsei.dto.response.cargoticketdetail;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Represents the response payload for cargo ticket detail operations.
 */
public class CargoTicketDetailResponse {
    private int cargoTicketDetailId;
    private int cargoTicketId;
    private int cargoTypePriceId;
    private String description;
    private int quantity;
    private BigDecimal weightKg;
    private BigDecimal dimensionVol;
    private BigDecimal calculatedPrice;
}
