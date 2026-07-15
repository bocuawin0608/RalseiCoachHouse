package com.ralsei.dto.response.passengerbooking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents the response payload for booking confirm operations.
 */
public record BookingConfirmResponse(
    String ticketCode,           
    String transactionId,       
    BigDecimal amount,           
    String bankAccountNumber,    
    String bankName,
    LocalDateTime paymentExpiresAt,
    String cancelToken
) {}