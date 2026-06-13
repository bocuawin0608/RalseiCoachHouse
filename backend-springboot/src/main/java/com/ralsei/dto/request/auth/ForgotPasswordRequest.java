package com.ralsei.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {
    @NotBlank(message = "Phone number is required")
    private String phone;

    // After have the OTP confirmed provide this:
    private String otp;
    private String newPassword;
}