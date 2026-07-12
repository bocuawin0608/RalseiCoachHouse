package com.ralsei.dto.response.customer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CustomerListResponse
 */

public record CustomerListResponse(
    Integer customerId,
    String customerName,
    String phone,
    String email,
    LocalDate dob,
    @JsonProperty("active") boolean isActive,
    LocalDateTime createdAt,
    Integer accountId,
    Long totalTrips,
    BigDecimal totalSpent,
    LocalDateTime lastBooking
) {}
