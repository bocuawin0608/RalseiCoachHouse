package com.ralsei.dto.response.staff;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StaffListResponse(
    Integer staffId,
    String staffName,
    String phone,
    String email,
    String cccd,
    String staffPosition,
    Integer ticketAgencyId,
    String ticketAgencyName,
    String username,
    @JsonProperty("active") boolean isActive,
    LocalDate dob,
    LocalDate hireDate,
    LocalDateTime createdAt
) {}
