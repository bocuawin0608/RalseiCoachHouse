package com.ralsei.dto.request.staff;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record UpdateStaffRequest(
    @NotBlank(message = "Tên nhân viên không được để trống.")
    String staffName,
    String phone,
    String email,
    LocalDate dob,
    String cccd,
    @NotBlank(message = "Chức vụ không được để trống.")
    String staffPosition,
    LocalDate hireDate,
    Integer ticketAgencyId,
    Boolean isActive
) {}
