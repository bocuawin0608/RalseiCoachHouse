package com.ralsei.dto.response.staffpassengerticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StaffPassengerTransferCandidateResponse(
    Integer tripId,
    String routeName,
    String coachTypeName,
    LocalDateTime departureTime,
    BigDecimal seatPrice,
    Integer availableSeats,
    Integer totalSeats
) {}
