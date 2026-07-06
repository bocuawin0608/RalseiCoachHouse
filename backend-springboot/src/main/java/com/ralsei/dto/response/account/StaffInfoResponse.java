package com.ralsei.dto.response.account;

import java.time.LocalDate;

/**
 * StaffInfoResponse
 */

public record StaffInfoResponse(
    Integer staffId,
    String staffName,
    String phone,
    String email,
    String cccd,
    LocalDate dob,
    String staffPosition,
    Integer ticketAgencyId,
    LocalDate hireDate,
    boolean isActive
) {}
