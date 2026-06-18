package com.ralsei.dto.response.coach;

import com.ralsei.model.enums.CoachStatus;
public record CoachResponse(
    Integer coachId,
    String licensePlate,
    String coachTypeName,
    String manufacturerAndYear,
    Long totalSeat,
    CoachStatus status
) {}
