package com.ralsei.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInRequest {
    @NotBlank(message = "Username (phone) is required")
    private String username; // Số điện thoại
    @NotBlank(message = "Password is required")
    private String password;
}
