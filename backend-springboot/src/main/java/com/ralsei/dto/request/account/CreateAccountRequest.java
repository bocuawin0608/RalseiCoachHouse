package com.ralsei.dto.request.account;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * CreateAccountRequest
 */

public record CreateAccountRequest(
    @NotBlank(message = "Tên đăng nhập không được để trống.")
    @Size(max = 50, message = "Tên đăng nhập không được vượt quá 50 ký tự.")
    String username,

    @NotBlank(message = "Mật khẩu không được để trống.")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự.")
    String password,

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

    @Positive(message = "ID bến xe phải là số dương.")
    Integer ticketAgencyId,

    @NotNull(message = "Ngày vào làm không được để trống.")
    LocalDate hireDate,

    @NotNull(message = "Vui lòng chọn ít nhất một vai trò.")
    List<@Positive Integer> roleIds
) {}
