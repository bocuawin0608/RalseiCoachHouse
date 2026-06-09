package com.ralsei.dto.response.auth;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private String username;
    private List<String> roles;  
    private String accessToken;  
}
