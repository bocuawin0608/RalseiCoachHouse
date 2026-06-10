package com.ralsei.dto.response.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType; // "Bearer"
    private long expiresIn; // how many second will the token expried
    private Integer accountId;
    private String username;
    
}
