/**
 * Service interface for trip staff cargo operations.
 * Manages on-coach cargo hand-off: load and unload only.
 * Destination receipt (DELIVERED) belongs to ticket staff.
 */
package com.ralsei.service.tripstaff;

import com.ralsei.dto.response.tripstaff.TripStaffCargoResponse;

/**
 * Provides the business service contract for trip staff cargo.
 */
public interface TripStaffCargoService {

    TripStaffCargoResponse getCargoList(String authorizationHeader, int tripId);

    void loadCargo(String authorizationHeader, int tripId, int cargoTicketId);

    void unloadCargo(String authorizationHeader, int tripId, int cargoTicketId);
}
