package com.ralsei.dto.request.staffpassengerticket;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StaffPassengerItineraryChangeRequest(
    Integer newTripId,
    @NotNull @Min(1) Integer pickupStopId,
    @NotNull @Min(1) Integer dropoffStopId,
    List<@Min(1) Integer> newTripSeatIds
) {}
