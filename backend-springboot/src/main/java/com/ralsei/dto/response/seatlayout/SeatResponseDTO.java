package com.ralsei.dto.response.seatlayout;

public record SeatResponseDTO(
    Integer seatId,
    String seatCode,
    Integer rowIndex,
    Integer colIndex,
    Boolean isActive
) {}
