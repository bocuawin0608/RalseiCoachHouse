/**
 * Response payload returned after a successful passenger check-in.
 * Contains passenger details and optionally the accompanied child info.
 */
package com.ralsei.dto.response.tripstaff;

public record CheckInResponse(
        Integer ticketDetailId,
        String fullName,
        String seatCode,
        String pickupStopName,
        String dropoffStopName,
        String status,
        AccompaniedChildResponse accompaniedChild
) {}
