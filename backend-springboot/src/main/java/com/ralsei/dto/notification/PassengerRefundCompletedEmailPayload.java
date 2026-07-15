package com.ralsei.dto.notification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable data required to render a passenger refund completion email after
 * ticket manager confirms that the refund amount was paid out.
 *
 * @param ticket original ticket data shown to identify the refunded booking
 * @param refundAmount amount confirmed as refunded to the customer
 * @param refundTime server timestamp when staff confirmed payout
 * @param transactionId payout reference recorded during confirmation
 * @param refundMethod outbound refund channel used for the payout
 */
/**
 * Provides the passenger refund completed email payload component for the application.
 */
public record PassengerRefundCompletedEmailPayload(
    PassengerTicketEmailPayload ticket,
    BigDecimal refundAmount,
    LocalDateTime refundTime,
    String transactionId,
    String refundMethod
) {}
