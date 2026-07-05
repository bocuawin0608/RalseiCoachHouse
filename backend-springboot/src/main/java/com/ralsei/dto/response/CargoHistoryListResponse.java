package com.ralsei.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CargoHistoryListResponse(
    Integer cargoTicketId,
    String ticketCode,
    String status,
    String senderName,
    String senderPhone,
    String receiverName,
    String receiverPhone,
    BigDecimal totalPrice,
    LocalDateTime createdAt,
    String pickupStopName,
    String dropoffStopName,
    String tripRouteName,
    LocalDateTime tripDepartureTime
) {}
