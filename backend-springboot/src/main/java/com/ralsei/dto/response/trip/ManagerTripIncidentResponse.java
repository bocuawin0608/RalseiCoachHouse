package com.ralsei.dto.response.trip;

import java.time.LocalDateTime;

/** Persistent manager alert for an unrecoverable coach incident. */
public record ManagerTripIncidentResponse(
        Integer tripId,
        String routeName,
        String licensePlate,
        LocalDateTime departureTime
) {}
