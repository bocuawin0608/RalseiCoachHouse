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
 * Result of batch-assigning waiting cargo orders onto one trip.
 */
public class CargoTripAssignResponse {
    private int tripId;
    private int assignedCount;
    private BigDecimal usedCargoVolume;
    private BigDecimal cargoCapacity;
    private List<CargoTicketResponse> assignedTickets;
}
