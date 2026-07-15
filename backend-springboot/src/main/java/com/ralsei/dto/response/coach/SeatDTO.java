package com.ralsei.dto.response.coach;

/**
 * Represents the data transfer object for seat.
 */
public record SeatDTO(
    Integer seatId,
    String seatCode,
    Integer rowIndex,
    Integer colIndex,
    Integer floorIndex,
    Boolean isActive
) {}
