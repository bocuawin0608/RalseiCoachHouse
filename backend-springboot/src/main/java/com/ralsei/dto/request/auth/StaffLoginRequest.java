package com.ralsei.dto.request.auth;

import com.ralsei.util.StringNormalize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents the request payload for staff login operations.
 */
public record StaffLoginRequest(
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(max = 50, message = "Tên đăng nhập không được vượt quá 50 ký tự.")
    String username,

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(max = 72, message = "Mật khẩu không được vượt quá 72 ký tự.")
    String password
) {
    public StaffLoginRequest {
        username = StringNormalize.trimToEmpty(username);
    }
}
