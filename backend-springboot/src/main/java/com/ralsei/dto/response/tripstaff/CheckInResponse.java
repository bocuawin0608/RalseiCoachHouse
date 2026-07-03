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
