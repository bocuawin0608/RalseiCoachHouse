package com.ralsei.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record CustomerLoginRequest(
    @NotBlank(message = "Firebase token không được để trống")
    String idToken,

    @NotBlank(message = "Username không được để trống")
    String username
) {}