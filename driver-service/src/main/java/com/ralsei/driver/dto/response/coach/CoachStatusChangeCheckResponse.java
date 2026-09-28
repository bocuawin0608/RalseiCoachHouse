package com.ralsei.driver.dto.response.coach;

/**
 * Represents the response payload for coach status change check operations.
 */
public record CoachStatusChangeCheckResponse(
    boolean allowed,
    String message,
    long upcomingTripCount
) {}
