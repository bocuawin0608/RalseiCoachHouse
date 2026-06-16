package com.ralsei.dto.request.coach;

import com.ralsei.model.enums.CoachStatus;

public record CoachUpdateInfoRequest(
    Integer routeId,
    Integer coachTypeId,
    String licensePlate,
    String manufacturer,
    Integer year,
    CoachStatus status
) {}
