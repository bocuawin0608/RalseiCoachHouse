package com.ralsei.staff.dto.response.ticketagency;

/**
 * CoachStopDropdownDTO
 */

/**
 * Represents the data transfer object for coach stop dropdown.
 */
public record CoachStopDropdownDTO(
    Integer stopPointId,
    String stopPointName,
    String address,
    String city
) {}
