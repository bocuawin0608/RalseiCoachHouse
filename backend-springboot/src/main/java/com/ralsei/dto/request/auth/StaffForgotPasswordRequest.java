package com.ralsei.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Staff forgot-password request from the internal login page.
 * Username and staff email are both required so the endpoint can verify the
 * account without exposing staff IDs or allowing browser-supplied account IDs.
 *
 * @param username local staff login username
 * @param email staff profile email that must belong to the same account
 */
public record StaffForgotPasswordRequest(
    @NotBlank(message = "Tên đăng nhập không được để trống.")
    @Size(max = 50, message = "Tên đăng nhập không được vượt quá 50 ký tự.")
    String username,

    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email không hợp lệ.")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự.")
    String email
) {}
