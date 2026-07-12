package com.ralsei.dto.request.staff;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

/**
 * UpdateStaffRequest
 */

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
    Boolean isActive,
    List<Integer> roleIds
) {}
