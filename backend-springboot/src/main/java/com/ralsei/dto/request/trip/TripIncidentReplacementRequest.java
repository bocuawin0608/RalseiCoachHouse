package com.ralsei.dto.request.trip;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Manager selection used to dispatch a replacement coach for an incident trip. */
public record TripIncidentReplacementRequest(
        @NotNull @Positive Integer routeId,
        @NotNull @Positive Integer coachId,
        @NotNull @Positive Integer driverId) {
}
