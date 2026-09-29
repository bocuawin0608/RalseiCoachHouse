package com.ralsei.staff.dto.response.cargoticketdetail;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response payload for cargo ticket detail price operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class CargoTicketDetailPriceResponse {
    private BigDecimal calculatedPrice;
}
