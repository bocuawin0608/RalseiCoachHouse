package com.ralsei.dto.response.seatlayout;

public record SeatDTO(
    Integer seatId,
    String seatCode,
    Integer rowIndex,
    Integer colIndex,
    Boolean isActive
) {}
