package com.ralsei.dto.response.coach;

public record CoachStatusChangeCheckResponse(
    boolean allowed,
    String message,
    long upcomingTripCount
) {}
