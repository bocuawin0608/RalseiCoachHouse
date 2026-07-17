package com.ralsei.dto.response.cargoticket;

import com.ralsei.dto.response.PagedResponse;

import lombok.Builder;
import lombok.Value;

/**
 * Destination-office context and one page of coaches awaiting cargo receipt.
 */
@Value
@Builder
public class CargoReceivingTripPageResponse {
    Integer ticketAgencyId;
    String ticketAgencyName;
    Integer stopPointId;
    String stopPointName;
    String city;
    PagedResponse<CargoReceivingTripResponse> trips;
}
