package com.ralsei.dto.response.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CustomerDetailResponse(
    Integer customerId,
    String customerName,
    String phone,
    String email,
    LocalDate dob,
    @JsonProperty("active") boolean isActive,
    LocalDateTime createdAt,
    Integer createdBy,
    LocalDateTime updatedAt,
    Integer updatedBy,
    Integer accountId,
    Long totalTrips,
    BigDecimal totalSpent,
    LocalDateTime lastBooking,
    List<CustomerBookingHistory> bookings
) {

    public record CustomerBookingHistory(
        Long passengerTicketId,
        String ticketCode,
        LocalDateTime createdAt,
        BigDecimal totalPrice,
        String tripCode,
        String status
    ) {}
}
