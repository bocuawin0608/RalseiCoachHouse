package com.ralsei.dto.response.coachtype;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ralsei.model.enums.CoachTypePriceStatus;

/**
 * Represents the response payload for coach type price operations.
 */
public record CoachTypePriceResponse(
    Integer coachTypePriceId,
    BigDecimal seatPrice,
    LocalDateTime startEffectiveDate,
    LocalDateTime endEffectiveDate,
    CoachTypePriceStatus status
) {}
