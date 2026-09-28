package com.ralsei.staff.dto.response.cargoticket;

import com.ralsei.staff.dto.response.PagedResponse;

import lombok.Builder;
import lombok.Value;

/**
 * Agency context and one page of coaches eligible to receive cargo there.
 */
@Value
@Builder
public class CargoOperationalTripPageResponse {
    Integer ticketAgencyId;
    String ticketAgencyName;
    Integer stopPointId;
    String stopPointName;
    String city;
    PagedResponse<CargoOperationalTripResponse> trips;
}
