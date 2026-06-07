package com.ralsei.dto.response.coachtype;

import java.math.BigDecimal;

public record CoachTypeDetailResponse(
    Integer coachTypeId,
    String coachTypeName,
    Integer totalSeat,
    BigDecimal currentPrice,
    Boolean isActive,
    String seatLayout
) {}
