/**
 * A single passenger entry on the trip dashboard,
 * including check-in status and optional accompanied child.
 */
package com.ralsei.dto.response.tripstaff;

/**
 * Represents the response payload for trip staff passenger operations.
 */
public record TripStaffPassengerResponse(
        Integer ticketDetailId,
        String fullName,
        String phone,
        String seatCodeSnapshot,
        String pickupStopName,
        String dropoffStopName,
        String status,
        AccompaniedChildResponse accompaniedChild
) {}
