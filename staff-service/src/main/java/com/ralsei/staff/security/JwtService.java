package com.ralsei.staff.security;

public interface JwtService {
    String extractUsername(String token);
    boolean isTokenValid(String token);
}
