package com.ralsei.driver.dto.response.coachtype;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ralsei.driver.model.CoachTypePriceStatus;

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
