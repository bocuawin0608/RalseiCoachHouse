package com.ralsei.dto.response.cargoticketdetail;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Represents the response payload for cargo ticket detail price operations.
 */
public class CargoTicketDetailPriceResponse {
    private BigDecimal calculatedPrice;
}
