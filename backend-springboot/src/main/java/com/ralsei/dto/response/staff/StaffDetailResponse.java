package com.ralsei.dto.response.staff;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StaffDetailResponse(
    Integer staffId,
    Integer accountId,
    Integer ticketAgencyId,
    String ticketAgencyName,
    String staffName,
    String phone,
    String email,
    LocalDate dob,
    String cccd,
    String staffPosition,
    LocalDate hireDate,
    @JsonProperty("active") boolean isActive,
    Boolean accountActive,
    String username,
    LocalDateTime createdAt,
    Integer createdBy,
    LocalDateTime updatedAt,
    Integer updatedBy
) {}
