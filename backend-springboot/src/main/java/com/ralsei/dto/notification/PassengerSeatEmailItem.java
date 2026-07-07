package com.ralsei.dto.notification;

/**
 * Passenger and boarding information for one seat displayed in the ticket email.
 *
 * @param seatCode seat label printed to the customer, for example {@code 13}
 * @param passengerName passenger assigned to the seat
 * @param passengerPhone passenger contact number
 * @param boardingToken opaque token rendered as an inline QR image; never expose
 *                      it in logs
 */
public record PassengerSeatEmailItem(
    String seatCode,
    String passengerName,
    String passengerPhone,
    String boardingToken
) {}
