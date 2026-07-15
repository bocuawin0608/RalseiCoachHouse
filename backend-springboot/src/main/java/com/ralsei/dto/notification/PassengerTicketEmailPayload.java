package com.ralsei.dto.notification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable data required to render a paid passenger-ticket confirmation email.
 * Keeping this DTO independent from JPA entities prevents lazy-loading and
 * transaction-lifecycle concerns in the mail delivery layer.
 *
 * @param ticketCode public booking reference shown to the customer
 * @param transactionId payment-provider transaction reference
 * @param paidAt time at which the payment was confirmed
 * @param routeName customer-facing route name
 * @param coachTypeName coach category assigned to the trip
 * @param coachLicensePlate full plate used to derive the four-digit coach number
 * @param departureTime scheduled departure from the first route stop
 * @param arrivalTime estimated arrival at the customer's drop-off stop
 * @param pickupStopName pickup location captured when the ticket was booked
 * @param dropoffStopName drop-off location captured when the ticket was booked
 * @param pickupPresentBy estimated time the coach reaches the pickup stop
 * @param primaryFullName lead passenger receiving the confirmation
 * @param primaryPhone lead passenger contact number
 * @param primaryEmail destination address for the confirmation
 * @param totalPrice final paid ticket total
 * @param seats immutable email details for every booked seat
 */
/**
 * Provides the passenger ticket email payload component for the application.
 */
public record PassengerTicketEmailPayload(
    String ticketCode,
    String transactionId,
    LocalDateTime paidAt,
    String routeName,
    String coachTypeName,
    String coachLicensePlate,
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
