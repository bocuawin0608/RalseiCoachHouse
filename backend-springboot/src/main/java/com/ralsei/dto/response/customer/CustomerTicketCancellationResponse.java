package com.ralsei.dto.response.customer;

import java.math.BigDecimal;

/** Result returned after a cancellation and refund request are committed. */
public record CustomerTicketCancellationResponse(
    String ticketCode,
    String ticketStatus,
    BigDecimal refundAmount,
    String refundStatus
) {}
