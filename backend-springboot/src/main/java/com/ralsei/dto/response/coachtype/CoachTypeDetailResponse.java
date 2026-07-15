package com.ralsei.dto.response.coachtype;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents the response payload for coach type detail operations.
 */
public record CoachTypeDetailResponse(
    Integer coachTypeId,
    String coachTypeName,
    Integer totalSeat,
    BigDecimal currentPrice,
    Boolean isActive,
    String seatLayout,
    Integer activeCoachCount,
    LocalDateTime currentPriceEffectiveFrom,
    Boolean canEditSeatLayout
) {}
