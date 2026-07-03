package com.ralsei.dto.response.coachtype;

import java.util.List;

import com.ralsei.model.enums.CoachStatus;

public record CoachTypeDeactivationCheckResponse(
    boolean canDeactivate,
    List<ActiveCoachSummary> activeCoaches
) {
    public record ActiveCoachSummary(
        Integer coachId,
        String licensePlate,
        CoachStatus status,
        long upcomingTripCount
    ) {}
}
