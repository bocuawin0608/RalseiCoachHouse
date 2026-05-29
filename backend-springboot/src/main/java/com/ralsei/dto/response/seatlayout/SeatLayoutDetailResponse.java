package com.ralsei.dto.response.seatlayout;

import java.math.BigDecimal;
import java.util.List;

public record SeatLayoutDetailResponse(
    Integer seatLayoutId,
    String seatLayoutName,
    Integer totalSeat,
    BigDecimal currentPrice,
    Boolean isActive,
    List<SeatDTO> seats
) {}
