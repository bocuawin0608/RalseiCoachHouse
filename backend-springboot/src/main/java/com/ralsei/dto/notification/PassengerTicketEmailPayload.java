package com.ralsei.dto.notification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PassengerTicketEmailPayload(
    String ticketCode,
    String transactionId,
    LocalDateTime paidAt,
    String routeName,
    String coachTypeName,
    LocalDateTime departureTime,
    LocalDateTime arrivalTime,
    String pickupStopName,
    String dropoffStopName,
    LocalDateTime pickupPresentBy,
    String primaryFullName,
    String primaryPhone,
    String primaryEmail,
    BigDecimal totalPrice,
    List<PassengerSeatEmailItem> seats
) {}
