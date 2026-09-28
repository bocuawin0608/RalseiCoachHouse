package com.ralsei.driver.dto.response.coach;

import com.ralsei.driver.model.CoachStatus;

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
