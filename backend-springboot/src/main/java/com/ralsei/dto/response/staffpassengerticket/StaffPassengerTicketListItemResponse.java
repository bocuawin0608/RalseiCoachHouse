package com.ralsei.dto.response.staffpassengerticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record StaffPassengerTicketListItemResponse(
    Integer passengerTicketId,
    String ticketCode,
    String status,
    String primaryPassengerName,
    String primaryPhone,
    String routeName,
    LocalDateTime departureTime,
    String licensePlate,
    List<String> seatCodes,
    int seatCount,
    BigDecimal totalPrice
) {}
