package com.ralsei.dto.request.staffpassengerticket;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StaffPassengerItineraryChangeRequest(
    Integer newTripId,
    @NotNull @Min(1) Integer pickupStopId,
    @NotNull @Min(1) Integer dropoffStopId,
    @Size(max = 10, message = "Chỉ được thay đổi tối đa 10 ghế trong một lần.")
    List<@Min(1) Integer> newTripSeatIds
) {}
