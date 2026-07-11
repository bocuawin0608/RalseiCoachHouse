package com.ralsei.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token is required")
    @Size(max = 512, message = "Refresh token không hợp lệ.")
    private String refreshToken;
}
