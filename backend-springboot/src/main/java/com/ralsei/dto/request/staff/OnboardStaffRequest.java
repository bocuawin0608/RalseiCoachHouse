package com.ralsei.dto.request.staff;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * OnboardStaffRequest
 */

/**
 * Represents the request payload for onboard staff operations.
 */
public record OnboardStaffRequest(
    @NotBlank(message = "Tên nhân viên không được để trống.")
    @Size(max = 100, message = "Tên nhân viên không được vượt quá 100 ký tự.")
    String staffName,

    @NotBlank(message = "Số điện thoại không được để trống.")
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự.")
    String phone,

    String email,
    LocalDate dob,
    String cccd,

    @NotBlank(message = "Chức vụ không được để trống.")
    String staffPosition,

    @NotNull(message = "Ngày vào làm không được để trống.")
    LocalDate hireDate,

    @Positive(message = "ID bến xe phải là số dương.")
    Integer ticketAgencyId,

    @NotNull(message = "Vui lòng chọn ít nhất một vai trò.")
    List<@Positive Integer> roleIds
) {}
