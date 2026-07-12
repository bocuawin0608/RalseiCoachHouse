package com.ralsei.dto.response.ticketagency;

/**
 * CoachStopDropdownDTO
 */

public record CoachStopDropdownDTO(
    Integer stopPointId,
    String stopPointName,
    String address,
    String city
) {}
