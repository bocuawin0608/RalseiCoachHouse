package com.ralsei.dto.response.tripstaff;

import java.time.LocalDateTime;

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
