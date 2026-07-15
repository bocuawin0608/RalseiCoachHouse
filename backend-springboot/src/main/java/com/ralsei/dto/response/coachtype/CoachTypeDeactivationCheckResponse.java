package com.ralsei.dto.response.coachtype;

import java.util.List;

import com.ralsei.model.enums.CoachStatus;

/**
 * Represents the response payload for coach type deactivation check operations.
 */
public record CoachTypeDeactivationCheckResponse(
    boolean canDeactivate,
    List<ActiveCoachSummary> activeCoaches
) {
    /**
     * Provides the active coach summary component for the application.
     */
    public record ActiveCoachSummary(
        Integer coachId,
        String licensePlate,
        CoachStatus status,
        long upcomingTripCount
    ) {}
}
