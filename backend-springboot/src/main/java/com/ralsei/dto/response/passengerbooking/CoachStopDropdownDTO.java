package com.ralsei.dto.response.passengerbooking;

/**
 * Represents the data transfer object for coach stop dropdown.
 */
public record CoachStopDropdownDTO(
    Integer stopPointId,
    String stopPointName,
    Integer minutesFromStart
) {}
