package com.ralsei.driver.dto.response.coach;

import java.util.List;

import com.ralsei.driver.model.CoachStatus;

/**
 * Represents the response payload for coach view detail operations.
 */
public record CoachViewDetailResponse(
    Integer coachId,
    String routeName,
    String coachTypeName,
    String licensePlate,
    String manufacturer,
    Integer year,
    CoachStatus status,
    Integer totalActiveSeats,
    List<SeatDTO> seats
) {}
