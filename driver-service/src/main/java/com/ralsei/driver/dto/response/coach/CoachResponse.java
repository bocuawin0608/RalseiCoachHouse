package com.ralsei.driver.dto.response.coach;

import com.ralsei.driver.model.CoachStatus;
/**
 * Represents the response payload for coach operations.
 */
public record CoachResponse(
    Integer coachId,
    String licensePlate,
    String coachTypeName,
    String manufacturerAndYear,
    Long totalSeat,
    CoachStatus status
) {}
