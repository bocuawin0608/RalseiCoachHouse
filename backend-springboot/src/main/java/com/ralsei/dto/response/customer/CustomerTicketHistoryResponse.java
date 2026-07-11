package com.ralsei.dto.response.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Customer-safe booking history data returned by the authenticated API. */
public record CustomerTicketHistoryResponse(
    Integer passengerTicketId,
    String ticketCode,
    String status,
    BigDecimal totalPrice,
    String pickupStopName,
    String dropoffStopName,
    LocalDateTime bookedAt,
    LocalDateTime departureTime,
    String routeName,
    String coachTypeName,
    String paymentMethod,
    String paymentStatus,
    String transactionId,
    LocalDateTime paymentExpiresAt,
    String fullName,
    String phone,
    String email,
    List<CustomerTicketSeatResponse> seats
) {
    /** A purchased seat; its QR image is fetched separately after authorization. */
    public record CustomerTicketSeatResponse(
        Integer ticketDetailId,
        String seatCode,
        BigDecimal price
    ) {}
}
