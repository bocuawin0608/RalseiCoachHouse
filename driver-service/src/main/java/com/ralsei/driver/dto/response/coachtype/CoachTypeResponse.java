package com.ralsei.driver.dto.response.coachtype;

import java.math.BigDecimal;

/**
 * Represents the response payload for coach type operations.
 */
public record CoachTypeResponse(
    Integer coachTypeId,
    String coachTypeName,
    Integer totalSeat,
    BigDecimal currentPrice,
    Boolean isActive,
    Integer totalCoach
) {}
