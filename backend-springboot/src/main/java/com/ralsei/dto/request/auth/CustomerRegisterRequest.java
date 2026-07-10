package com.ralsei.dto.request.auth;

import com.ralsei.util.StringNormalize;
import com.ralsei.util.validation.BookingValidationPatterns;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRegisterRequest(
    @NotBlank(message = "Firebase token không được để trống")
    @Size(max = 4096, message = "Firebase token không hợp lệ.")
    String idToken,

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(max = 50, message = "Số điện thoại không được vượt quá 50 ký tự.")
    @Pattern(
        regexp = BookingValidationPatterns.PHONE,
        message = "Số điện thoại không hợp lệ."
    )
    String username,

    @NotBlank(message = "Họ và tên không được để trống.")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự.")
    @Pattern(
        regexp = BookingValidationPatterns.FULL_NAME,
        message = "Họ và tên không hợp lệ."
    )
    String customerName,

    @NotBlank(message = "Email không được để trống.")
    @Size(max = BookingValidationPatterns.EMAIL_MAX_LENGTH, message = "Email không được vượt quá 254 ký tự.")
    @Pattern(
        regexp = BookingValidationPatterns.EMAIL,
        message = "Email không hợp lệ."
    )
    String email
) {
    public CustomerRegisterRequest {
        username = StringNormalize.trimToEmpty(username);
        customerName = StringNormalize.trimToEmpty(customerName);
        email = StringNormalize.trimToEmpty(email);
    }
}
