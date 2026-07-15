package com.ralsei.dto.response.coach;

import com.ralsei.model.enums.CoachStatus;

/**
 * Represents the response payload for coach edit form operations.
 */
public record CoachEditFormResponse(
    Integer coachId,
    Integer routeId,
    Integer coachTypeId,
    String licensePlate,
    String manufacturer,
    Integer year,
    CoachStatus status
) {}
