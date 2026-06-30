package com.ralsei.dto.response.passengerbooking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingPaymentPageResponse(
    String ticketCode,
    String transactionId,
    BigDecimal amount,
    String bankAccountNumber,
    String bankName,
    LocalDateTime paymentExpiresAt,
    String paymentStatus,
    String primaryPassengerName,
    String primaryPassengerPhone,
    List<String> seatCodes,
    Integer tripId
) {}
