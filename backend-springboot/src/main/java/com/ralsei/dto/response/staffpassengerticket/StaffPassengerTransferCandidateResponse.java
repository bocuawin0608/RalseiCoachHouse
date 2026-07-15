package com.ralsei.dto.response.staffpassengerticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents the response payload for staff passenger transfer candidate operations.
 */
public record StaffPassengerTransferCandidateResponse(
    Integer tripId,
    String routeName,
    String coachTypeName,
    LocalDateTime departureTime,
    BigDecimal seatPrice,
    Integer availableSeats,
    Integer totalSeats
) {}
