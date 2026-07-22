/**
 * Service interface for trip staff passenger operations.
 * Defines check-in workflows (QR and manual) and dashboard retrieval.
 */
package com.ralsei.service.tripstaff;

import java.time.LocalDate;
import java.util.List;

import com.ralsei.dto.projection.tripstaff.AssignedTripProjection;
import com.ralsei.dto.request.tripstaff.QrCheckInRequest;
import com.ralsei.dto.response.tripstaff.CheckInResponse;
import com.ralsei.dto.response.tripstaff.TripStaffDashboardResponse;

/**
 * Provides the business service contract for trip staff passenger.
 */
public interface TripStaffPassengerService {

    List<AssignedTripProjection> getAssignedTrips(String authorizationHeader, LocalDate date);

    TripStaffDashboardResponse getDashboard(String authorizationHeader, Integer tripId);

    CheckInResponse checkInByQr(String authorizationHeader, Integer tripId, QrCheckInRequest request);

    CheckInResponse checkInManual(String authorizationHeader, Integer tripId, Integer ticketDetailId);

    void startTrip(String authorizationHeader, Integer tripId);

    void endTrip(String authorizationHeader, Integer tripId);

    void reportUnrecoverableIncident(String authorizationHeader, Integer tripId);

    void markNoShow(String authorizationHeader, Integer tripId, Integer ticketDetailId);
}
