package com.ralsei.dto.request.account;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * UpdateAccountRequest
 */

public record UpdateAccountRequest(
    @NotBlank(message = "Tên nhân viên không được để trống.")
    @Size(max = 100, message = "Tên nhân viên không được vượt quá 100 ký tự.")
    String staffName,

    @NotBlank(message = "Số điện thoại không được để trống.")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự.")
    String phone,

    String email,

    String cccd,

    LocalDate dob,

    @NotBlank(message = "Chức vụ không được để trống.")
    String staffPosition,

    Integer ticketAgencyId,

    LocalDate hireDate,

    Boolean isActive
) {}
