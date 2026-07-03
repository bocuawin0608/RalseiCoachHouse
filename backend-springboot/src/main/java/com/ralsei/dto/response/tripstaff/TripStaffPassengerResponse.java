package com.ralsei.dto.response.tripstaff;

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
