package com.ralsei.dto.response.coachtype;

import java.math.BigDecimal;

public record CoachTypeResponse(
    Integer coachTypeId,
    String coachTypeName,
    Integer totalSeat,
    BigDecimal currentPrice,
    Boolean isActive,
    Integer totalCoach
) {}
