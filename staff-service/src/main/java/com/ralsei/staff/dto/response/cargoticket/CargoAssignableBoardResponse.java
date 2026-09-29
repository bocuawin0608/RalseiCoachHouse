package com.ralsei.staff.dto.response.cargoticket;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Assign board payload: trip capacity snapshot plus eligible unassigned orders.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CargoAssignableBoardResponse {
    private int tripId;
    private BigDecimal usedCargoVolume;
    private BigDecimal cargoCapacity;
    private List<CargoAssignableTicketResponse> tickets;
}
