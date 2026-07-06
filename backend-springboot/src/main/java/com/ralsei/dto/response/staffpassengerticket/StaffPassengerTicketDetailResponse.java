package com.ralsei.dto.response.staffpassengerticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record StaffPassengerTicketDetailResponse(
    Integer passengerTicketId,
    String ticketCode,
    String status,
    Integer tripId,
    Integer routeId,
    String routeName,
    LocalDateTime departureTime,
    String licensePlate,
    String coachTypeName,
    String pickupStopName,
    String dropoffStopName,
    BigDecimal totalPrice,
    String voucherCodeSnapshot,
    String soldByStaffName,
    LocalDateTime bookedAt,
    String paymentMethod,
    String paymentStatus,
    BigDecimal paymentAmount,
    BigDecimal paymentRefundAmount,
    List<RefundItem> refunds,
    List<SeatItem> seats,
    List<String> allowedActions,
    Long hoursUntilDeparture,
    String refundTierLabel
) {
    public record SeatItem(
        Integer ticketDetailId,
        String seatCode,
        String status,
        BigDecimal price,
        String fullName,
        String phone,
        String email,
        String childFullname,
        Integer childBirthYear
    ) {}

    public record RefundItem(
        Integer refundId,
        BigDecimal amount,
        String status,
        String reason,
        LocalDateTime refundTime
    ) {}
}
