package com.ralsei.dto.notification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Immutable data required to render a passenger-ticket cancellation email.
 * The embedded ticket payload keeps route, passenger, and seat formatting
 * consistent with the paid-ticket confirmation email while cancellation fields
 * describe the refund state created by the cancel workflow.
 *
 * @param ticket original ticket data shown to identify the cancelled booking
 * @param cancelledAt time when the cancellation request was committed
 * @param refundAmount amount queued for refund to the customer
 * @param refundStatus current refund workflow status, for example {@code PENDING}
 * @param cancellationReason short customer-safe reason displayed in the email
 */
/**
 * Provides the passenger ticket cancellation email payload component for the application.
 */
public record PassengerTicketCancellationEmailPayload(
    PassengerTicketEmailPayload ticket,
    LocalDateTime cancelledAt,
    BigDecimal refundAmount,
    String refundStatus,
    String cancellationReason
) {}
