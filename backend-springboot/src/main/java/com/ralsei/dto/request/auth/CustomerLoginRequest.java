package com.ralsei.dto.request.auth;

import com.ralsei.util.StringNormalize;
import com.ralsei.util.validation.BookingValidationPatterns;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerLoginRequest(
    @NotBlank(message = "Firebase token không được để trống")
    @Size(max = 4096, message = "Firebase token không hợp lệ.")
    String idToken,

    @NotBlank(message = "Username không được để trống")
    @Size(max = 50, message = "Số điện thoại không được vượt quá 50 ký tự.")
    @Pattern(
        regexp = BookingValidationPatterns.PHONE,
        message = "Số điện thoại không hợp lệ."
    )
    String username
) {
    public CustomerLoginRequest {
        username = StringNormalize.trimToEmpty(username);
    }
}
