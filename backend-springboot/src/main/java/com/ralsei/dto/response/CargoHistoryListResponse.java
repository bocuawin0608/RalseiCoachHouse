package com.ralsei.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Summary record for a cargo order displayed in the customer's history list.
 */
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
