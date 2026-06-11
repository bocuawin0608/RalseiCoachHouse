package com.ralsei.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record StaffLoginRequest(
    @NotBlank(message = "Tên đăng nhập không được để trống")
    String username,

    @NotBlank(message = "Mật khẩu không được để trống")
    String password
) {}