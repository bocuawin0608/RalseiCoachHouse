package com.ralsei.dto.response.passengerbooking;

public record CoachStopDropdownDTO(
    Integer stopPointId,
    String stopPointName,
    Integer minutesFromStart
) {}
