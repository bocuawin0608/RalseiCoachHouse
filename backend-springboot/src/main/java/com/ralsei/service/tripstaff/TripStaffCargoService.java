/**
 * Service interface for trip staff cargo operations.
 * Manages the cargo lifecycle: load, unload, and mark as delivered.
 */
package com.ralsei.service.tripstaff;

import com.ralsei.dto.response.tripstaff.TripStaffCargoResponse;

public interface TripStaffCargoService {

    TripStaffCargoResponse getCargoList(String authorizationHeader, int tripId);

    void loadCargo(String authorizationHeader, int tripId, int cargoTicketId);

    void unloadCargo(String authorizationHeader, int tripId, int cargoTicketId);

    void markDelivered(String authorizationHeader, int tripId, int cargoTicketId);
}
