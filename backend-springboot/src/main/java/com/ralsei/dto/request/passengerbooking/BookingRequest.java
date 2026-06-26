package com.ralsei.dto.request.passengerbooking;

import java.util.List;

public record BookingRequest(
    Integer tripId,
    Integer customerId,
    Integer voucherId,
    Integer pickupStopId,
    Integer dropoffStopId,
    List<PassengerDTO> passengers
) {}
