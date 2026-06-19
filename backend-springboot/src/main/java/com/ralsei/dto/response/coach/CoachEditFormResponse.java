package com.ralsei.dto.response.coach;

import com.ralsei.model.enums.CoachStatus;

public record CoachEditFormResponse(
    Integer coachId,
    Integer routeId,
    Integer coachTypeId,
    String licensePlate,
    String manufacturer,
    Integer year,
    CoachStatus status
) {}
