package com.ralsei.dto.response.auth;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private final boolean success;
    private final String message;
    private final String username;
    private final List<String> roles;  
    private final String accessToken;  
    private final String refreshToken;
}
