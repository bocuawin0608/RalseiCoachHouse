package com.ralsei.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record CustomerRegisterRequest(
    @NotBlank(message = "Firebase token không được để trống")
    String idToken,

    @NotBlank(message = "Số điện thoại không được để trống")
    String username,        

    String customerName,    
    String email            
) {}