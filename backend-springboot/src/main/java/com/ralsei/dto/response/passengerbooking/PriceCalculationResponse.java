package com.ralsei.dto.response.passengerbooking;

import java.math.BigDecimal;

public record PriceCalculationResponse(
    BigDecimal basePrice,
    BigDecimal baseSurcharge,
    BigDecimal totalRawPrice,
    BigDecimal discountAmount,
    BigDecimal totalFinalPrice
) {}
