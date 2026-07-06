package com.ralsei.service.tripstaff;

import java.time.LocalDate;
import java.util.List;

import com.ralsei.dto.projection.tripstaff.AssignedTripProjection;
import com.ralsei.dto.request.tripstaff.QrCheckInRequest;
import com.ralsei.dto.response.tripstaff.CheckInResponse;
import com.ralsei.dto.response.tripstaff.TripStaffDashboardResponse;

public interface TripStaffPassengerService {

    List<AssignedTripProjection> getAssignedTrips(String authorizationHeader, LocalDate date);

    TripStaffDashboardResponse getDashboard(String authorizationHeader, Integer tripId);

    CheckInResponse checkInByQr(String authorizationHeader, Integer tripId, QrCheckInRequest request);

    CheckInResponse checkInManual(String authorizationHeader, Integer tripId, Integer ticketDetailId);
}
