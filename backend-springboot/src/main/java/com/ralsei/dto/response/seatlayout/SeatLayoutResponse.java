package com.ralsei.dto.response.seatlayout;

import java.math.BigDecimal;

public record SeatLayoutResponse(
    Integer seatLayoutId,
    String seatLayoutName,
    Integer totalSeat,
    BigDecimal currentPrice,
    Boolean isActive
) {}