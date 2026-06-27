package com.ralsei.dto.request.passengerbooking;

import java.util.List;

public record BookingConfirmRequest(
    Integer voucherId,
    Integer pickupStopId,
    Integer dropoffStopId,
    List<PassengerDTO> passengers
) {}
