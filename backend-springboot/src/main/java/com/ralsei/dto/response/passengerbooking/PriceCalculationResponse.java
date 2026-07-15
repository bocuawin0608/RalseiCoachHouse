package com.ralsei.dto.response.passengerbooking;

import java.math.BigDecimal;

/**
 * Represents the response payload for price calculation operations.
 */
public record PriceCalculationResponse(
    BigDecimal basePrice,
    BigDecimal baseSurcharge,
    BigDecimal totalRawPrice,
    BigDecimal discountAmount,
    BigDecimal totalFinalPrice
) {}
