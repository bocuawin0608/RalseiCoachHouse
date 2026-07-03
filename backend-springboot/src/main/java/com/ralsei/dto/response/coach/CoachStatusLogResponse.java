package com.ralsei.dto.response.coach;

import java.time.LocalDateTime;

import com.ralsei.model.enums.CoachStatus;

public record CoachStatusLogResponse(
    Integer coachStatusLogId,
    CoachStatus fromStatus,
    CoachStatus toStatus,
    String reason,
    LocalDateTime expectedEndAt,
    LocalDateTime createdAt
) {}
