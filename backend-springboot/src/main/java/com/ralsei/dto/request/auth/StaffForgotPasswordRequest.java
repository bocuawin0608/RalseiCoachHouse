package com.ralsei.dto.request.auth;

import com.ralsei.util.StringNormalize;
import com.ralsei.util.validation.BookingValidationPatterns;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Size(max = BookingValidationPatterns.EMAIL_MAX_LENGTH, message = "Email không được vượt quá 254 ký tự.")
    @Pattern(
        regexp = BookingValidationPatterns.EMAIL,
        message = "Email không hợp lệ."
    )
    String email
) {
    public StaffForgotPasswordRequest {
        username = StringNormalize.trimToEmpty(username);
        email = StringNormalize.trimToEmpty(email);
    }
}
