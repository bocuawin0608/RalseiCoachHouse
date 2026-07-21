package com.ralsei.dto.response.cargoticket;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Assign board payload: trip capacity snapshot plus eligible unassigned orders.
 */
public class CargoAssignableBoardResponse {
    private int tripId;
    private BigDecimal usedCargoVolume;
    private BigDecimal cargoCapacity;
    private List<CargoAssignableTicketResponse> tickets;
}
