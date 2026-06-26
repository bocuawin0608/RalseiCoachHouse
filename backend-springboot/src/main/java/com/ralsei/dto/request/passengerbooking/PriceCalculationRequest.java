package com.ralsei.dto.request.passengerbooking;

public record PriceCalculationRequest(
    String holdToken,
    Integer pickupStopId,
    Integer dropoffStopId,
    Integer voucherId
) {}
