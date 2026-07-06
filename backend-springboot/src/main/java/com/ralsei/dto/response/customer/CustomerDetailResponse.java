package com.ralsei.dto.response.customer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * CustomerDetailResponse
 */

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
    Integer updatedBy
) {}
