package com.ralsei.dto.response.coach;

public record CoachResponse(
    String licensePlate,
    String coachTypeName,
    String manufacturerAndYear,
    Integer totalSeat,
    String status
) {}
