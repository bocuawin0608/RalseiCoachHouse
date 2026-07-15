package com.ralsei.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ResetPasswordRequest
 */

/**
 * Represents the request payload for reset password operations.
 */
public record ResetPasswordRequest(
    @NotBlank(message = "Mật khẩu mới không được để trống.")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự.")
    String newPassword
) {}
