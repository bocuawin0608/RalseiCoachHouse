package com.ralsei.dto.response.staffpassengerticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the response payload for staff passenger ticket detail operations.
 */
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
    Integer pickupStopId,
    Integer dropoffStopId,
    BigDecimal totalPrice,
    String voucherCodeSnapshot,
    String soldByStaffName,
    LocalDateTime bookedAt,
    LocalDateTime updatedAt,
    String updatedByStaffName,
    String paymentMethod,
    String paymentStatus,
    BigDecimal paymentAmount,
    BigDecimal paymentRefundAmount,
    List<RefundItem> refunds,
    List<SeatItem> seats,
    List<String> allowedActions,
    Long hoursUntilDeparture,
    String refundTierLabel,
    String majorChangeType,
    LocalDateTime refundPolicyDepartureTime
) {
    /**
     * Provides the seat item component for the application.
     */
    public record SeatItem(
        Integer ticketDetailId,
        Integer tripSeatId,
        String seatCode,
        String status,
        BigDecimal price,
        String fullName,
        String phone,
        String email,
        String childFullname,
        Integer childBirthYear
    ) {}

    /**
     * Provides the refund item component for the application.
     */
    public record RefundItem(
        Integer refundId,
        BigDecimal amount,
        String status,
        String reason,
        LocalDateTime refundTime
    ) {}
}
