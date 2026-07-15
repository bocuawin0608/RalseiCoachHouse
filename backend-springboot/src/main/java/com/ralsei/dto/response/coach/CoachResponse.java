package com.ralsei.dto.response.coach;

import com.ralsei.model.enums.CoachStatus;
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
