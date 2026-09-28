package com.ralsei.driver.dto.response.coach;

import java.time.LocalDateTime;

import com.ralsei.driver.model.CoachStatus;

/**
 * Represents the response payload for coach status log operations.
 */
public record CoachStatusLogResponse(
    Integer coachStatusLogId,
    CoachStatus fromStatus,
    CoachStatus toStatus,
    String reason,
    LocalDateTime expectedEndAt,
    LocalDateTime createdAt
) {}
