package com.ralsei.dto.response.coach;

public record SeatDTO(
    Integer seatId,
    String seatCode,
    Integer rowIndex,
    Integer colIndex,
    Integer floorIndex,
    Boolean isActive
) {}
