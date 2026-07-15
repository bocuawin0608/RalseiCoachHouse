/**
 * Summary information for a trip displayed on the staff dashboard,
 * including route, departure time, vehicle, and check-in progress.
 */
package com.ralsei.dto.response.tripstaff;

import java.time.LocalDateTime;

/**
 * Represents the response payload for trip staff summary operations.
 */
public record TripStaffSummaryResponse(
        Integer tripId,
        String routeName,
        LocalDateTime departureTime,
        String licensePlate,
        String coachTypeName,
        String tripStatus,
        String assignedRole,
        int checkedInCount,
        int totalPassengers
) {}
